import { describe, it, expect, vi, beforeEach } from "vitest";
import { mount, flushPromises } from "@vue/test-utils";
import CaptchaModal from "../CaptchaModal.vue";

const mockGetCaptcha = vi.fn();
vi.mock("@/api/common", () => ({
  getCaptcha: () => mockGetCaptcha(),
}));

describe("CaptchaModal.vue", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const globalStubs = {
    "wd-popup": {
      template: "<div class='wd-popup-stub' v-if='modelValue'><slot /></div>",
      props: ["modelValue"],
    },
  };

  it("fetches captcha when opened", async () => {
    mockGetCaptcha.mockResolvedValue({
      captchaKey: "test-key-1",
      captchaBase64: "data:image/png;base64,mockedImage",
    });

    const wrapper = mount(CaptchaModal, {
      props: {
        modelValue: true,
      },
      global: {
        stubs: globalStubs,
      },
    });

    await flushPromises();

    expect(mockGetCaptcha).toHaveBeenCalledTimes(1);
    const img = wrapper.find(".captcha-image");
    expect(img.exists()).toBe(true);
    expect(img.attributes("src")).toBe("data:image/png;base64,mockedImage");
  });

  it("does not emit confirm if code is empty or shorter than 4 digits", async () => {
    mockGetCaptcha.mockResolvedValue({
      captchaKey: "test-key-1",
      captchaBase64: "data:image/png;base64,mockedImage",
    });

    const wrapper = mount(CaptchaModal, {
      props: {
        modelValue: true,
      },
      global: {
        stubs: globalStubs,
      },
    });

    await flushPromises();

    // Click confirm when input is empty
    const confirmBtn = wrapper.find(".dialog-btn.confirm");
    await confirmBtn.trigger("click");
    expect(wrapper.emitted("confirm")).toBeFalsy();
  });

  it("emits confirm with captchaKey and captchaCode when input is valid", async () => {
    mockGetCaptcha.mockResolvedValue({
      captchaKey: "test-key-2",
      captchaBase64: "data:image/png;base64,mockedImage2",
    });

    const wrapper = mount(CaptchaModal, {
      props: {
        modelValue: true,
      },
      global: {
        stubs: globalStubs,
      },
    });

    await flushPromises();

    const input = wrapper.find(".captcha-input");
    await input.setValue("A8K2");

    const confirmBtn = wrapper.find(".dialog-btn.confirm");
    await confirmBtn.trigger("click");

    expect(wrapper.emitted("confirm")).toBeTruthy();
    expect(wrapper.emitted("confirm")![0]).toEqual([
      { captchaKey: "test-key-2", captchaCode: "A8K2" },
    ]);
  });

  it("emits update:modelValue false when cancel is clicked", async () => {
    mockGetCaptcha.mockResolvedValue({
      captchaKey: "test-key-3",
      captchaBase64: "data:image/png;base64,mockedImage3",
    });

    const wrapper = mount(CaptchaModal, {
      props: {
        modelValue: true,
      },
      global: {
        stubs: globalStubs,
      },
    });

    await flushPromises();

    const cancelBtn = wrapper.find(".dialog-btn.cancel");
    await cancelBtn.trigger("click");

    expect(wrapper.emitted("update:modelValue")).toBeTruthy();
    expect(wrapper.emitted("update:modelValue")![0]).toEqual([false]);
  });
});
