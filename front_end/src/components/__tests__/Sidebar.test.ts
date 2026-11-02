import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import Sidebar from '../Sidebar.vue';
import * as api from '@/api/common';

vi.mock('@/api/common', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/common')>();
  return {
    ...actual,
    createAccountBook: vi.fn(),
    updateAccountBook: vi.fn(),
    logout: vi.fn(),
  };
});

vi.mock('@/stores/themeStore', () => ({
  useTheme: () => ({ themeMode: { value: 'dark' } }),
}));

vi.mock('@/stores/transactionStore', () => ({
  useTransactions: () => ({
    load: vi.fn(),
    clear: vi.fn(),
  }),
}));

vi.mock('@/stores/categoryStore', () => ({
  useCategories: () => ({
    clear: vi.fn(),
  }),
}));

vi.mock('@/utils/sync', () => ({
  fullSync: vi.fn().mockResolvedValue({ success: true }),
}));

describe('Sidebar.vue - Account Book with Custom Emoji', () => {
  const mockBooks = [
    { id: '101', name: '日常账本', coverIcon: '🏠', type: 0 },
    { id: '102', name: '旅行账本', coverIcon: '', type: 0 }, // 无自定义icon，走兜底
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  const defaultProps = {
    visible: true,
    books: mockBooks,
    currentBookId: '101',
    userInfo: {
      id: '1',
      username: 'test',
      email: 'test@example.com',
      nickname: '测试用户',
      avatar: '',
      phone: '',
    },
  };

  it('renders books with custom emoji and fallback letter', () => {
    const wrapper = mount(Sidebar, {
      props: defaultProps,
      global: {
        stubs: {
          'wd-popup': {
            template: '<div><slot /></div>',
            props: ['modelValue'],
          },
          'wd-icon': true,
        },
      },
    });

    const bookIcons = wrapper.findAll('.book-icon-text');
    expect(bookIcons.length).toBe(2);
    // 第一个为自定义 emoji
    expect(bookIcons[0].text()).toBe('🏠');
    expect(bookIcons[0].classes()).toContain('emoji-char');
    // 第二个兜底为首字
    expect(bookIcons[1].text()).toBe('旅');
    expect(bookIcons[1].classes()).not.toContain('emoji-char');
  });

  it('supports selecting emoji and creates account book with coverIcon', async () => {
    const wrapper = mount(Sidebar, {
      props: defaultProps,
      global: {
        stubs: {
          'wd-popup': {
            template: '<div><slot /></div>',
            props: ['modelValue'],
          },
          'wd-icon': true,
        },
      },
    });

    vi.mocked(api.createAccountBook).mockResolvedValueOnce({
      id: '103',
      name: '投资账本',
      coverIcon: '💰',
      type: 0,
    });

    // 打开新增弹窗
    await wrapper.find('.section-add-btn').trigger('click');

    // 输入账本名称
    const nameInput = wrapper.find('.dialog-input');
    await nameInput.setValue('投资账本');

    // 点击预设 emoji 💰 (第6个)
    const emojiItems = wrapper.findAll('.emoji-item');
    const moneyEmoji = emojiItems.find((item) => item.text() === '💰');
    expect(moneyEmoji).toBeTruthy();
    await moneyEmoji!.trigger('click');

    // 确认创建
    await wrapper.find('.dialog-btn.confirm').trigger('click');

    expect(api.createAccountBook).toHaveBeenCalledWith({
      name: '投资账本',
      coverIcon: '💰',
      type: 0,
    });
    expect(wrapper.emitted('update:books')).toBeTruthy();
  });

  it('supports fallback to letter when no emoji is selected', async () => {
    const wrapper = mount(Sidebar, {
      props: defaultProps,
      global: {
        stubs: {
          'wd-popup': {
            template: '<div><slot /></div>',
            props: ['modelValue'],
          },
          'wd-icon': true,
        },
      },
    });

    vi.mocked(api.createAccountBook).mockResolvedValueOnce({
      id: '104',
      name: '家庭账本',
      coverIcon: '',
      type: 0,
    });

    await wrapper.find('.section-add-btn').trigger('click');
    const nameInput = wrapper.find('.dialog-input');
    await nameInput.setValue('家庭账本');

    // 不选 emoji，直接确认
    await wrapper.find('.dialog-btn.confirm').trigger('click');

    expect(api.createAccountBook).toHaveBeenCalledWith({
      name: '家庭账本',
      coverIcon: undefined,
      type: 0,
    });
  });

  it('supports editing book name and emoji or clearing it back to fallback', async () => {
    const wrapper = mount(Sidebar, {
      props: defaultProps,
      global: {
        stubs: {
          'wd-popup': {
            template: '<div><slot /></div>',
            props: ['modelValue'],
          },
          'wd-icon': true,
        },
      },
    });

    vi.mocked(api.updateAccountBook).mockResolvedValueOnce({
      id: '101',
      name: '我的日常',
      coverIcon: '',
      type: 0,
    });

    // 开启编辑模式
    await wrapper.find('.section-edit-btn').trigger('click');

    // 点击编辑第一个账本
    const firstBook = wrapper.findAll('.book-item')[0];
    await firstBook.trigger('click');

    // 弹窗中回显了原 emoji '🏠'
    expect(wrapper.find('.preview-text').text()).toBe('🏠');

    // 点击使用默认重置 emoji
    await wrapper.find('.emoji-reset-btn').trigger('click');

    // 输入新名称
    const nameInput = wrapper.find('.dialog-input');
    await nameInput.setValue('我的日常');

    // 预览已恢复为首字兜底 '我'
    expect(wrapper.find('.preview-text').text()).toBe('我');

    // 确认保存
    await wrapper.find('.dialog-btn.confirm').trigger('click');

    expect(api.updateAccountBook).toHaveBeenCalledWith('101', {
      name: '我的日常',
      coverIcon: '', // 传空字符串恢复兜底
    });
  });
});
