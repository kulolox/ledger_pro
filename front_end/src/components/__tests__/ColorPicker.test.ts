import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import ColorPicker from '../ColorPicker.vue';

// Mock pinia theme
vi.mock('@/stores/themeStore', () => ({
  useTheme: () => ({ themeMode: { value: 'dark' } }),
}));

describe('ColorPicker.vue', () => {
  it('should render correctly and display hex value', async () => {
    const wrapper = mount(ColorPicker, {
      props: {
        visible: true,
        modelValue: '#FF4D4F',
      },
      global: {
        stubs: {
          'wd-popup': {
            template: '<div class="wd-popup-stub"><slot /></div>',
            props: ['modelValue', 'visible'],
          },
        },
      },
    });

    expect(wrapper.find('.picker-title').text()).toBe('自定义调色盘');
    const hexInput = wrapper.find('.hex-field');
    expect((hexInput.element as HTMLInputElement).value).toBe('FF4D4F');
  });

  it('should emit update:modelValue and confirm on handleConfirm', async () => {
    const wrapper = mount(ColorPicker, {
      props: {
        visible: true,
        modelValue: '#1677FF',
      },
      global: {
        stubs: {
          'wd-popup': {
            template: '<div><slot /></div>',
          },
        },
      },
    });

    await wrapper.find('.picker-confirm').trigger('click');
    expect(wrapper.emitted('update:modelValue')).toBeTruthy();
    expect(wrapper.emitted('confirm')).toBeTruthy();
    expect(wrapper.emitted('update:visible')?.[0]).toEqual([false]);
  });

  it('should emit cancel and close on handleCancel', async () => {
    const wrapper = mount(ColorPicker, {
      props: {
        visible: true,
        modelValue: '#1677FF',
      },
      global: {
        stubs: {
          'wd-popup': {
            template: '<div><slot /></div>',
          },
        },
      },
    });

    await wrapper.find('.picker-cancel').trigger('click');
    expect(wrapper.emitted('cancel')).toBeTruthy();
    expect(wrapper.emitted('update:visible')?.[0]).toEqual([false]);
  });

  it('should select swatch and update current color', async () => {
    const wrapper = mount(ColorPicker, {
      props: {
        visible: true,
        modelValue: '#1677FF',
      },
      global: {
        stubs: {
          'wd-popup': {
            template: '<div><slot /></div>',
          },
        },
      },
    });

    const swatches = wrapper.findAll('.swatch-item');
    expect(swatches.length).toBeGreaterThan(0);

    // 点击第一个 swatch (#FF4D4F)
    await swatches[0].trigger('click');
    const hexInput = wrapper.find('.hex-field');
    expect((hexInput.element as HTMLInputElement).value).toBe('FF4D4F');
  });
});
