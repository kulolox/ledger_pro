import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";
import NavBar from "../NavBar.vue";

function mountNav(props = {}, slots = {}) {
  return mount(NavBar, {
    props,
    slots,
    global: {
      components: {
        view: {
          template: '<div :class="$attrs.class" :style="$attrs.style"><slot /></div>',
          props: ["class", "style"],
          inheritAttrs: true,
        },
      },
    },
  });
}

describe("NavBar", () => {
  it("renders left, center, right slots", () => {
    const wrapper = mountNav(
      {},
      {
        left: '<span class="left-content">Back</span>',
        center: '<span class="center-content">Title</span>',
        right: '<span class="right-content">Menu</span>',
      },
    );
    expect(wrapper.find(".left-content").exists()).toBe(true);
    expect(wrapper.find(".center-content").exists()).toBe(true);
    expect(wrapper.find(".right-content").exists()).toBe(true);
  });

  it("has default rightWidth prop", () => {
    const wrapper = mountNav();
    expect(wrapper.props("rightWidth")).toBe("72rpx");
  });

  it("accepts custom rightWidth prop", () => {
    const wrapper = mountNav({ rightWidth: "120rpx" });
    expect(wrapper.props("rightWidth")).toBe("120rpx");
  });

  it("renders nav-header and nav-bar structure", () => {
    const wrapper = mountNav();
    expect(wrapper.find(".nav-header").exists()).toBe(true);
    expect(wrapper.find(".nav-bar").exists()).toBe(true);
  });

  it("applies status bar height in non-floating mode", () => {
    const wrapper = mountNav();
    const navBar = wrapper.find(".nav-bar");
    expect(navBar.attributes("style")).toContain("padding-top: calc(44px + 12rpx)");
  });

  it("applies status bar height in floating mode", () => {
    const wrapper = mountNav({ floating: true });
    const navHeader = wrapper.find(".nav-header");
    expect(navHeader.attributes("style")).toContain("padding-top: calc(44px + 8rpx)");
  });
});
