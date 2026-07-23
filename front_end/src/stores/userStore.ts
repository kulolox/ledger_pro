import { ref } from "vue";
import { select, insert, updateById, execute } from "@/utils/db";

interface UserProfile {
  id: number;
  nickname: string;
  created_at: string;
}

const nickname = ref("");
const hasProfile = ref(false);

export function useUser() {
  async function loadUser(): Promise<boolean> {
    try {
      const rows = await select<UserProfile>("SELECT * FROM user_profile LIMIT 1");
      if (rows.length > 0) {
        nickname.value = rows[0].nickname;
        hasProfile.value = true;
        return true;
      }
    } catch {
      // ignore
    }
    return false;
  }

  async function createUser(name: string): Promise<void> {
    await insert("user_profile", { nickname: name });
    nickname.value = name;
    hasProfile.value = true;
  }

  async function updateNickname(name: string): Promise<void> {
    const rows = await select<UserProfile>("SELECT id FROM user_profile LIMIT 1");
    if (rows.length > 0) {
      await updateById("user_profile", rows[0].id, { nickname: name });
      nickname.value = name;
    }
  }

  async function clearUser(): Promise<void> {
    await execute("DELETE FROM user_profile");
    hasProfile.value = false;
    nickname.value = "";
  }

  return { nickname, hasProfile, loadUser, createUser, updateNickname, clearUser };
}
