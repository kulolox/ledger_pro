/**
 * App 运行时环境全局垫片
 * uni-app 5+App (Weex/JSCore) 逻辑层缺少 window 和 self 对象
 * 为纯 JS 第三方库（如 node-forge 等）提供标准全局作用域映射
 */
if (typeof globalThis !== "undefined") {
  if (typeof (globalThis as any).self === "undefined") {
    (globalThis as any).self = globalThis;
  }
  if (typeof (globalThis as any).window === "undefined") {
    (globalThis as any).window = globalThis;
  }
}

export {};

