#!/usr/bin/env node

/**
 * LedgerPro Android 一键全自动化构建与打包脚本
 * 
 * 功能：
 * 1. 优先读取 .env.development 中的配置；若未配置则自动探测物理网卡局域网 IP
 * 2. 明确输出目标 API 地址及来源
 * 3. 自动构建前端 App 离线资源 (pnpm build:custom app)
 * 4. 自动同步资源到 Android 离线工程 (assets/apps/<appid>/www)
 * 5. 自动调用 Gradle 命令行构建 APK (gradlew assembleRelease / assembleDebug)
 * 6. 自动归档 APK 至 dist-apk/ 目录
 * 7. 可选真机自动推包与启动 (adb install -r)
 */

import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import { spawnSync, execSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

// 解决 Windows 控制台编码问题
if (process.platform === 'win32') {
  try {
    execSync('chcp 65001 >nul 2>&1');
  } catch (_) {}
}

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const ROOT_DIR = path.resolve(__dirname, '..');
const FRONT_END_DIR = path.resolve(ROOT_DIR, 'front_end');

// 终端颜色与样式（使用安全文本标记，避免 emoji 在非 UTF-8 控制台乱码）
const color = {
  reset: (text) => `\x1b[0m${text}\x1b[0m`,
  green: (text) => `\x1b[32m${text}\x1b[0m`,
  yellow: (text) => `\x1b[33m${text}\x1b[0m`,
  blue: (text) => `\x1b[34m${text}\x1b[0m`,
  cyan: (text) => `\x1b[36m${text}\x1b[0m`,
  red: (text) => `\x1b[31m${text}\x1b[0m`,
  bold: (text) => `\x1b[1m${text}\x1b[0m`,
  magenta: (text) => `\x1b[35m${text}\x1b[0m`,
};

function logStep(step, message) {
  console.log(`\n${color.cyan(color.bold(`[步骤 ${step}]`))} ${color.bold(message)}`);
}

function logSuccess(message) {
  console.log(`${color.green('[成功]')} ${message}`);
}

function logInfo(message) {
  console.log(`${color.blue('[信息]')} ${message}`);
}

function logWarn(message) {
  console.log(`${color.yellow('[警告]')} ${message}`);
}

function logError(message) {
  console.error(`${color.red('[错误]')} ${message}`);
}

// 帮助信息
function printHelp() {
  console.log(`
${color.bold('LedgerPro Android 一键自动化构建脚本')}

${color.bold('用法:')}
  node scripts/build-apk.mjs [选项]
  pnpm build:apk [选项] (在 front_end 目录下)

${color.bold('选项:')}
  --env <lan|prod|dev>      指定环境类型 (默认: lan 局域网开发环境)
  --api <url>               自定义 API 基地址 (优先级最高)
  --port <port>             指定局域网后端服务端口 (默认: 8114)
  --android <dir>           指定 Android 原生工程目录 (相对根目录或绝对路径)
  --mode <release|debug>    构建模式 (默认: release)
  --clean                   强制 Gradle 执行 clean 清理缓存后重新打包 (推荐)
  --no-clean                跳过 Gradle clean 增量打包
  --install                 构建完成后，自动通过 ADB 安装到已连接的手机设备
  --changelog <text>        指定更新日志内容 (默认: 自动提取最近 Git Commit 提交说明)
  --skip-build-front        跳过前端资源编译，直接复用现有的 dist/build/app 产物
  --skip-apk                只编译前端并装配到 Android assets，不执行 Gradle 构建 APK
  -h, --help                显示此帮助信息
`);
}

// 解析命令行参数
const args = process.argv.slice(2);
const options = {
  env: 'lan',
  api: '',
  port: 8114,
  androidPath: '',
  mode: 'release',
  clean: true, // 默认开启 clean，确保不吃 Gradle 缓存旧包
  install: false,
  changelog: '',
  skipBuildFront: false,
  skipApk: false,
};

for (let i = 0; i < args.length; i++) {
  const arg = args[i];
  if (arg === '-h' || arg === '--help') {
    printHelp();
    process.exit(0);
  } else if (arg === '--env' && args[i + 1]) {
    options.env = args[++i];
  } else if (arg === '--api' && args[i + 1]) {
    options.api = args[++i];
  } else if (arg === '--port' && args[i + 1]) {
    options.port = parseInt(args[++i], 10);
  } else if (arg === '--android' && args[i + 1]) {
    options.androidPath = args[++i];
  } else if (arg === '--mode' && args[i + 1]) {
    options.mode = args[++i].toLowerCase();
  } else if (arg === '--clean') {
    options.clean = true;
  } else if (arg === '--no-clean') {
    options.clean = false;
  } else if (arg === '--install') {
    options.install = true;
  } else if (arg === '--changelog' && args[i + 1]) {
    options.changelog = args[++i];
  } else if (arg === '--skip-build-front') {
    options.skipBuildFront = true;
  } else if (arg === '--skip-apk') {
    options.skipApk = true;
  }
}

// 安全解析 JSON（支持自动兼容 Windows 复制过来的单反斜杠路径）
function safeParseJson(content, filePath) {
  try {
    return JSON.parse(content);
  } catch (initialErr) {
    try {
      const sanitized = content.replace(/(["'])(?:(?=(\\?))\2.)*?\1/g, (match) => {
        return match.replace(/\\(?!["\\/bfnrtu])/g, '\\\\');
      });
      return JSON.parse(sanitized);
    } catch (_) {
      throw initialErr;
    }
  }
}

// 读取配置文件 (build.config.json 与 build.config.local.json)
function loadConfigs() {
  let config = {
    androidProjectPath: 'android',
    backendPort: 8114,
    defaultEnv: 'lan',
    apkOutputDir: 'dist-apk',
    autoInstallIfDeviceConnected: false,
  };

  const mainConfigPath = path.resolve(ROOT_DIR, 'build.config.json');
  if (fs.existsSync(mainConfigPath)) {
    try {
      const raw = fs.readFileSync(mainConfigPath, 'utf-8');
      const data = safeParseJson(raw, mainConfigPath);
      config = { ...config, ...data };
    } catch (e) {
      logError(`读取 build.config.json 语法错误 (请检查反斜杠或标点): ${e.message}`);
    }
  }

  const localConfigPath = path.resolve(ROOT_DIR, 'build.config.local.json');
  if (fs.existsSync(localConfigPath)) {
    try {
      const raw = fs.readFileSync(localConfigPath, 'utf-8');
      const localData = safeParseJson(raw, localConfigPath);
      config = { ...config, ...localData };
      logSuccess(`已加载本地覆盖配置: build.config.local.json`);
    } catch (e) {
      logError(`读取 build.config.local.json 语法错误: ${e.message}`);
    }
  }

  return config;
}

const config = loadConfigs();
if (!options.androidPath) {
  options.androidPath = config.androidProjectPath;
}
if (options.env === 'lan' && config.backendPort && !args.includes('--port')) {
  options.port = config.backendPort;
}
if (!args.includes('--install') && config.autoInstallIfDeviceConnected) {
  options.install = true;
}

// 探测局域网真实 IPv4（排除虚拟网卡）
function detectLanIp() {
  const interfaces = os.networkInterfaces();
  const candidates = [];

  const ignoreKeywords = [
    'virtual', 'vethernet', 'wsl', 'vmware', 'vbox', 'loopback',
    'pseudo', 'teredo', 'docker', 'hyper-v', 'container', 'bluetooth'
  ];

  for (const [name, addrs] of Object.entries(interfaces)) {
    const lowerName = name.toLowerCase();
    const isVirtual = ignoreKeywords.some(kw => lowerName.includes(kw));

    for (const addr of addrs || []) {
      if (addr.family === 'IPv4' || addr.family === 4) {
        if (!addr.internal && addr.address !== '127.0.0.1') {
          candidates.push({
            name,
            address: addr.address,
            isVirtual,
          });
        }
      }
    }
  }

  const physical = candidates.filter(c => !c.isVirtual);
  if (physical.length > 0) {
    const preferred = physical.find(c => /以太网|ethernet|eth|wlan|wi-fi|无线/i.test(c.name)) || physical[0];
    return { ip: preferred.address, name: preferred.name, all: candidates };
  }

  if (candidates.length > 0) {
    return { ip: candidates[0].address, name: candidates[0].name, all: candidates };
  }

  return { ip: '127.0.0.1', name: 'localhost', all: [] };
}

// 从 .env 文件中提取配置值
function extractEnvVar(envFilePath, key) {
  if (!fs.existsSync(envFilePath)) return null;
  const content = fs.readFileSync(envFilePath, 'utf-8');
  const reg = new RegExp(`^\\s*${key}\\s*=\\s*(.+)$`, 'm');
  const match = content.match(reg);
  if (match && match[1]) {
    const val = match[1].trim();
    return val.replace(/#.*$/, '').trim();
  }
  return null;
}

// 读取前端 manifest.json
function getManifestInfo() {
  const manifestPath = path.resolve(FRONT_END_DIR, 'src/manifest.json');
  if (!fs.existsSync(manifestPath)) {
    throw new Error(`找不到前端 manifest.json: ${manifestPath}`);
  }
  const content = fs.readFileSync(manifestPath, 'utf-8');
  try {
    const cleanJson = content.replace(/\/\*[\s\S]*?\*\/|([^:]|^)\/\/.*$/gm, '$1');
    const manifest = JSON.parse(cleanJson);
    return {
      appid: manifest.appid || '__UNI__LEDGERPRO',
      versionName: manifest.versionName || '1.0.0',
      versionCode: manifest.versionCode || 100,
      name: manifest.name || 'LedgerPro',
    };
  } catch (err) {
    const appidMatch = content.match(/"appid"\s*:\s*"([^"]+)"/);
    const versionMatch = content.match(/"versionName"\s*:\s*"([^"]+)"/);
    return {
      appid: appidMatch ? appidMatch[1] : '__UNI__LEDGERPRO',
      versionName: versionMatch ? versionMatch[1] : '1.0.0',
      versionCode: 100,
      name: 'LedgerPro',
    };
  }
}

// 递归拷贝文件夹
function copyDirSync(src, dest) {
  if (!fs.existsSync(dest)) {
    fs.mkdirSync(dest, { recursive: true });
  }
  const entries = fs.readdirSync(src, { withFileTypes: true });
  for (const entry of entries) {
    const srcPath = path.join(src, entry.name);
    const destPath = path.join(dest, entry.name);
    if (entry.isDirectory()) {
      copyDirSync(srcPath, destPath);
    } else {
      fs.copyFileSync(srcPath, destPath);
    }
  }
}

// 临时环境配置现场保护与恢复（使用 .env.production.local，在 Vite build 时拥有最高优先级）
const envTargetFilePath = path.resolve(FRONT_END_DIR, '.env.production.local');
let originalEnvContent = null;
let envFileExisted = false;

function setupEnvCleanup(targetUrl) {
  if (fs.existsSync(envTargetFilePath)) {
    envFileExisted = true;
    originalEnvContent = fs.readFileSync(envTargetFilePath, 'utf-8');
  }

  // 写入临时目标 API 配置
  const content = `# 自动构建临时生成 - 构建完成后自动还原\nVITE_API_BASE_URL=${targetUrl}\n`;
  fs.writeFileSync(envTargetFilePath, content, 'utf-8');

  // 注册清理恢复钩子
  const restoreEnv = () => {
    try {
      if (envFileExisted) {
        fs.writeFileSync(envTargetFilePath, originalEnvContent, 'utf-8');
      } else if (fs.existsSync(envTargetFilePath)) {
        fs.unlinkSync(envTargetFilePath);
      }
    } catch (_) {}
  };

  process.on('exit', restoreEnv);
  process.on('SIGINT', () => {
    restoreEnv();
    process.exit(130);
  });
  process.on('uncaughtException', (err) => {
    restoreEnv();
    logError(`发生未捕获异常: ${err.message}`);
    process.exit(1);
  });

  return restoreEnv;
}

// 执行主流程
async function main() {
  console.log(`\n==================================================================`);
  console.log(`${color.bold(color.blue('>>> LedgerPro Android 一键全自动化构建流水线 <<<'))}`);
  console.log(`==================================================================`);

  // 1. 解析目标 API 地址与来源
  logStep('1/5', '解析目标环境与 API 地址');
  let targetApiUrl = '';
  let apiSourceDesc = '';

  if (options.api) {
    targetApiUrl = options.api;
    apiSourceDesc = '命令行指定 (--api)';
  } else if (options.env === 'prod') {
    // 优先读取本地私有 .env.production.local，其次读取 .env.production
    const localProdEnvPath = path.resolve(FRONT_END_DIR, '.env.production.local');
    const prodEnvPath = path.resolve(FRONT_END_DIR, '.env.production');
    
    const localProdUrl = extractEnvVar(localProdEnvPath, 'VITE_API_BASE_URL');
    const configuredProdUrl = extractEnvVar(prodEnvPath, 'VITE_API_BASE_URL');
    
    if (localProdUrl) {
      targetApiUrl = localProdUrl;
      apiSourceDesc = '本地私有生产配置 (front_end/.env.production.local)';
    } else if (configuredProdUrl) {
      targetApiUrl = configuredProdUrl;
      apiSourceDesc = '生产配置 (front_end/.env.production)';
    } else {
      logError('生产 API 地址为空！请在 front_end/.env.production.local 配置或通过 --api 参数指定');
      restoreEnv();
      process.exit(1);
    }
  } else {
    // 优先读取 front_end/.env.development 中是否配置了非空地址
    const devEnvPath = path.resolve(FRONT_END_DIR, '.env.development');
    const configuredDevUrl = extractEnvVar(devEnvPath, 'VITE_API_BASE_URL');

    if (configuredDevUrl) {
      targetApiUrl = configuredDevUrl;
      apiSourceDesc = '研发配置 (front_end/.env.development)';
    } else {
      // 自动嗅探物理局域网 IP
      const { ip, name } = detectLanIp();
      targetApiUrl = `http://${ip}:${options.port}`;
      apiSourceDesc = `物理网卡自动探测 [${name}] (端口: ${options.port})`;
    }
  }

  // 醒目标识打包的目标 API 地址横幅
  console.log(`\n------------------------------------------------------------------`);
  console.log(`${color.bold('【当前打包后端 API】')} : ${color.bold(color.green(targetApiUrl))}`);
  console.log(`${color.bold('【后端 API 来源】')}   : ${color.cyan(apiSourceDesc)}`);
  console.log(`------------------------------------------------------------------\n`);

  // 设置临时环境变量并确保恢复
  const restoreEnv = setupEnvCleanup(targetApiUrl);
  logSuccess(`已临时写入 front_end/.env.local (构建结束后自动还原清理)`);

  // 2. 读取项目与 AppID 信息
  const manifest = getManifestInfo();
  logInfo(`应用信息: ${color.bold(manifest.name)} | AppID: ${color.cyan(manifest.appid)} | 版本: ${color.yellow(`v${manifest.versionName}`)} (Code: ${manifest.versionCode})`);

  // 3. 构建前端 App 离线包
  logStep('2/5', '编译前端 App 离线静态资源');
  const frontDistAppDir = path.resolve(FRONT_END_DIR, 'dist/build/app');

  if (options.skipBuildFront) {
    logWarn(`已跳过前端编译 (--skip-build-front)，使用现有的 dist/build/app 产物`);
  } else {
    // 编译前清空旧产物，避免文件缓存
    if (fs.existsSync(frontDistAppDir)) {
      fs.rmSync(frontDistAppDir, { recursive: true, force: true });
    }
    console.log(`正在执行: pnpm build:custom app ... (请稍候)`);
    const isWindows = process.platform === 'win32';
    const npmCmd = isWindows ? 'pnpm.cmd' : 'pnpm';

    const buildRes = spawnSync(npmCmd, ['build:custom', 'app'], {
      cwd: FRONT_END_DIR,
      stdio: 'inherit',
      shell: true,
      env: {
        ...process.env,
        JAVA_TOOL_OPTIONS: '-Dfile.encoding=UTF-8',
      },
    });

    if (buildRes.status !== 0) {
      restoreEnv();
      logError(`前端编译失败 (Exit code: ${buildRes.status})`);
      process.exit(1);
    }
  }

  if (!fs.existsSync(frontDistAppDir)) {
    restoreEnv();
    logError(`未找到前端编译产物目录: ${frontDistAppDir}`);
    process.exit(1);
  }

  // 严格校验产物中实际打入的 API 地址是否匹配目标
  const appServiceJsPath = path.resolve(frontDistAppDir, 'app-service.js');
  if (fs.existsSync(appServiceJsPath)) {
    const jsContent = fs.readFileSync(appServiceJsPath, 'utf-8');
    if (!jsContent.includes(targetApiUrl)) {
      restoreEnv();
      logError(`致命错误：产物中打入的 API 地址与目标 (${targetApiUrl}) 不一致！已自动终止。`);
      process.exit(1);
    }
  }
  logSuccess(`前端资源编译成功且 API 核验通过: ${color.green(targetApiUrl)}`);

  // 4. 装配到 Android 原生工程
  logStep('3/5', '装配离线静态资源到 Android 原生工程');
  let androidResolvedDir = path.isAbsolute(options.androidPath)
    ? options.androidPath
    : path.resolve(ROOT_DIR, options.androidPath);

  logInfo(`目标 Android 工程路径: ${color.yellow(androidResolvedDir)}`);

  const androidAssetsAppsDir = path.resolve(androidResolvedDir, 'app/src/main/assets/apps');
  const targetWwwDir = path.resolve(androidAssetsAppsDir, manifest.appid, 'www');

  if (!fs.existsSync(androidResolvedDir)) {
    logWarn(`Android 原生工程目录未找到: ${androidResolvedDir}`);
    console.log(`
${color.yellow('提示：')}
可在根目录的 build.config.json 或 build.config.local.json 中指定正确路径：
{
  "androidProjectPath": "D:/WorkSpace/UniPlugin-Hello-AS"
}
`);
    restoreEnv();
    process.exit(1);
  }

  // 清理旧 www 并完整拷贝
  if (fs.existsSync(targetWwwDir)) {
    fs.rmSync(targetWwwDir, { recursive: true, force: true });
  }
  fs.mkdirSync(targetWwwDir, { recursive: true });
  copyDirSync(frontDistAppDir, targetWwwDir);
  logSuccess(`前端资源已成功装配至: ${targetWwwDir}`);

  // 友好校验 dcloud_control.xml
  const controlXmlPath = path.resolve(androidResolvedDir, 'app/src/main/assets/data/dcloud_control.xml');
  if (fs.existsSync(controlXmlPath)) {
    const xmlContent = fs.readFileSync(controlXmlPath, 'utf-8');
    if (!xmlContent.includes(manifest.appid)) {
      logWarn(`警告: dcloud_control.xml 中的 appid 与 manifest.json (${manifest.appid}) 不一致，可能导致白屏！`);
    }
  }

  // 5. 调用 Gradle 命令行构建 APK
  logStep('4/5', '执行 Gradle 构建 APK');

  if (options.skipApk) {
    logWarn(`已跳过 APK 构建 (--skip-apk)`);
    restoreEnv();
    return;
  }

  const isWindows = process.platform === 'win32';
  const gradlewCmd = isWindows
    ? path.resolve(androidResolvedDir, 'gradlew.bat')
    : path.resolve(androidResolvedDir, 'gradlew');

  if (!fs.existsSync(gradlewCmd)) {
    logError(`在 Android 工程中未找到 gradlew: ${gradlewCmd}`);
    restoreEnv();
    process.exit(1);
  }

  // 构建前清空 Android 原生工程中的旧 APK，彻底杜绝混用上一次构建的残留文件
  const apkSearchDir = path.resolve(androidResolvedDir, 'app/build/outputs/apk', options.mode);
  if (fs.existsSync(apkSearchDir)) {
    const oldFiles = fs.readdirSync(apkSearchDir).filter(f => f.endsWith('.apk'));
    for (const f of oldFiles) {
      try {
        fs.unlinkSync(path.resolve(apkSearchDir, f));
      } catch (_) {}
    }
  }

  const targetTask = options.mode === 'debug' ? 'assembleDebug' : 'assembleRelease';
  const gradleTasks = options.clean ? ['clean', targetTask] : [targetTask];
  logInfo(`正在运行 Gradle 任务: ${color.cyan(gradleTasks.join(' '))} ...`);

  const gradleRes = spawnSync(gradlewCmd, gradleTasks, {
    cwd: androidResolvedDir,
    stdio: 'inherit',
    shell: true,
    env: {
      ...process.env,
      JAVA_TOOL_OPTIONS: '-Dfile.encoding=UTF-8',
      GRADLE_OPTS: '-Dfile.encoding=UTF-8',
    },
  });

  if (gradleRes.status !== 0) {
    restoreEnv();
    logError(`Gradle 构建 APK 失败 (Exit code: ${gradleRes.status})`);
    process.exit(1);
  }

  // 查找产出的最新 APK（按最后修改时间降序排序）
  let finalApkPath = null;
  if (fs.existsSync(apkSearchDir)) {
    const files = fs.readdirSync(apkSearchDir)
      .filter(f => f.endsWith('.apk'))
      .map(f => ({ name: f, path: path.resolve(apkSearchDir, f), mtime: fs.statSync(path.resolve(apkSearchDir, f)).mtimeMs }))
      .sort((a, b) => b.mtime - a.mtime);

    if (files.length > 0) {
      finalApkPath = files[0].path;
    }
  }

  if (!finalApkPath || !fs.existsSync(finalApkPath)) {
    logError(`未在 ${apkSearchDir} 找到新生成的 APK 文件（可能被构建任务跳过或出错）`);
    restoreEnv();
    process.exit(1);
  }

  // 归档 APK 到 dist-apk 目录
  const outputDistDir = path.resolve(ROOT_DIR, config.apkOutputDir || 'dist-apk');
  if (!fs.existsSync(outputDistDir)) {
    fs.mkdirSync(outputDistDir, { recursive: true });
  }

  const timestamp = new Date().toISOString().slice(0, 10).replace(/-/g, '');
  const outApkName = `LedgerPro-v${manifest.versionName}-${options.env}-${options.mode}-${timestamp}.apk`;
  const targetApkPath = path.resolve(outputDistDir, outApkName);

  fs.copyFileSync(finalApkPath, targetApkPath);
  const apkSizeMb = (fs.statSync(targetApkPath).size / (1024 * 1024)).toFixed(2);

  // 同步一份到后端的 APK 更新托管目录 (back_end/data/apk/)
  const backendApkDir = path.resolve(ROOT_DIR, 'back_end', 'data', 'apk');
  try {
    if (!fs.existsSync(backendApkDir)) {
      fs.mkdirSync(backendApkDir, { recursive: true });
    }
    fs.copyFileSync(finalApkPath, path.resolve(backendApkDir, outApkName));
  } catch {}

  // 动态提取更新日志：优先命令行传入，其次自动提取最近 Git Commit 提交说明
  function getGitRecentChangelog(limit = 5) {
    try {
      const gitOut = spawnSync('git', ['log', `-${limit}`, '--pretty=format:%s', '--no-merges'], {
        cwd: ROOT_DIR,
        encoding: 'utf-8',
      });
      if (gitOut.status !== 0 || !gitOut.stdout) return '';
      const lines = gitOut.stdout
        .split('\n')
        .map(l => l.trim())
        .filter(l => l && !l.startsWith('Merge ') && !l.startsWith('chore(release)'));
      if (lines.length === 0) return '';
      return lines.map((l, i) => `${i + 1}. ${l}`).join('\n');
    } catch {
      return '';
    }
  }

  const gitChangelog = getGitRecentChangelog(5);
  const finalChangelog = options.changelog || gitChangelog || '1. 日常功能优化与已知问题修复\n2. 提升系统性能与稳定性';

  // 自动写入 APK 发布元数据清单 latest.json (供给后端免重启热感知)
  try {
    const meta = {
      version: manifest.versionName,
      versionCode: parseInt(manifest.versionCode, 10) || 100,
      packageSize: `${apkSizeMb} MB`,
      apkFileName: outApkName,
      downloadUrl: '/api/v1/system/version/download',
      changelog: finalChangelog,
      buildTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
    };
    fs.writeFileSync(path.resolve(backendApkDir, 'latest.json'), JSON.stringify(meta, null, 2), 'utf-8');
    logSuccess('已自动生成发布元数据清单 back_end/data/apk/latest.json');
  } catch (err) {
    logWarn(`写入 latest.json 失败: ${err.message}`);
  }

  // 打印最终成果面板
  console.log(`\n==================================================================`);
  console.log(`${color.bold(color.green('>>> APK 打包与归档成功！ <<<'))}`);
  console.log(`  * 应用版本    : v${manifest.versionName} (${manifest.name})`);
  console.log(`  * 后端 API    : ${color.bold(color.green(targetApiUrl))} (${apiSourceDesc})`);
  console.log(`  * 输出文件    : ${color.bold(color.cyan(targetApkPath))}`);
  console.log(`  * 文件大小    : ${color.yellow(`${apkSizeMb} MB`)}`);
  console.log(`  * 更新日志    :`);
  finalChangelog.split('\n').forEach(line => console.log(`      ${color.cyan(line)}`));
  console.log(`==================================================================`);

  // 6. 可选真机自动安装
  logStep('5/5', '检查真机连接并安装 (ADB)');
  let shouldInstall = options.install;

  const adbCheck = spawnSync('adb', ['devices'], { encoding: 'utf-8' });
  const hasAdb = adbCheck.status === 0;

  if (hasAdb) {
    const outputLines = adbCheck.stdout.split('\n').filter(l => l.trim() && !l.startsWith('List of devices'));
    const onlineDevices = outputLines.filter(l => l.includes('device') && !l.includes('offline'));

    if (onlineDevices.length > 0) {
      logInfo(`检测到在线设备: ${color.green(onlineDevices[0].split('\t')[0])}`);
      if (shouldInstall) {
        logInfo(`正在通过 ADB 安装 APK 到手机: adb install -r ...`);
        const installRes = spawnSync('adb', ['install', '-r', targetApkPath], {
          stdio: 'inherit',
        });
        if (installRes.status === 0) {
          logSuccess(`应用安装成功！`);
          try {
            spawnSync('adb', ['shell', 'am', 'start', '-n', 'com.kulolox.ledgerpro/io.dcloud.PandoraEntry'], {
              stdio: 'ignore',
            });
            logSuccess(`已自动唤起手机 App！`);
          } catch (_) {}
        } else {
          logWarn(`ADB 安装失败，请检查手机是否允许 USB 安装或屏幕是否弹窗确认。`);
        }
      } else {
        logInfo(`提示: 加上 ${color.cyan('--install')} 参数即可自动将 APK 快速安装并打开在手机上！`);
      }
    } else {
      logInfo(`未检测到已连接的手机设备 (如需自动安装请开启手机 USB 调试并连接)`);
    }
  }

  // 恢复环境现场
  restoreEnv();
  console.log(`\n${color.bold(color.green('[完成] 打包全流程圆满结束！'))}\n`);
}

main().catch((err) => {
  logError(`构建异常退出: ${err.message}`);
  process.exit(1);
});
