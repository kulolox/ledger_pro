import json
import sys

def main():
    try:
        input_data = sys.stdin.read()
        if not input_data.strip():
            print(json.dumps({"decision": "allow"}))
            return
        payload = json.loads(input_data)
        tool_call = payload.get("toolCall", {})
        args = tool_call.get("args", {})
        cmd = args.get("CommandLine", "").strip().lower()

        # 高危命令拦截黑名单
        blocked_keywords = [
            "drop database",
            "rm -rf /",
            "git push --force",
            "git push -f",
            "format c:",
            ":(){ :|:& };:"
        ]

        for keyword in blocked_keywords:
            if keyword in cmd:
                print(json.dumps({
                    "decision": "deny",
                    "reason": f"安全拦截：检测到高危指令 '{keyword}'，已禁止执行！"
                }))
                return

        # 默认放行
        print(json.dumps({"decision": "allow"}))
    except Exception as e:
        # 异常兜底，放行并提示
        print(json.dumps({"decision": "allow"}))

if __name__ == "__main__":
    main()
