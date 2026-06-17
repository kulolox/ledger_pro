import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";
import NumberKeyboard from "../NumberKeyboard.vue";

function mountKB() {
  return mount(NumberKeyboard, {
    global: {
      components: {
        view: { template: "<div><slot /></div>", props: ["class", "style"] },
        text: { template: "<span><slot /></span>", props: ["class"] },
      },
    },
  });
}

describe("NumberKeyboard", () => {
  it("renders all 16 keys in the grid", () => {
    const wrapper = mountKB();
    expect(wrapper.findAll(".kb-key").length).toBe(16);
  });

  it("emits keyPress with correct value on num key click", () => {
    const wrapper = mountKB();
    const key1 = wrapper.findAll(".kb-key").at(8)!;
    key1.trigger("click");
    expect(wrapper.emitted("keyPress")?.[0]).toEqual(["1"]);
  });

  it("emits keyPress for digit 0", () => {
    const wrapper = mountKB();
    const key0 = wrapper.findAll(".kb-key").at(14)!;
    key0.trigger("click");
    expect(wrapper.emitted("keyPress")?.[0]).toEqual(["0"]);
  });

  it("emits keyPress for backspace", () => {
    const wrapper = mountKB();
    const bs = wrapper.findAll(".kb-key").at(3)!;
    bs.trigger("click");
    expect(wrapper.emitted("keyPress")?.[0]).toEqual(["backspace"]);
  });

  it("emits keyPress for operator + and -", () => {
    const wrapper = mountKB();
    const plus = wrapper.findAll(".kb-key").at(7)!;
    plus.trigger("click");
    expect(wrapper.emitted("keyPress")?.[0]).toEqual(["+"]);

    const minus = wrapper.findAll(".kb-key").at(11)!;
    minus.trigger("click");
    expect(wrapper.emitted("keyPress")?.[1]).toEqual(["-"]);
  });

  it("emits keyPress for clear", () => {
    const wrapper = mountKB();
    const clear = wrapper.findAll(".kb-key").at(12)!;
    clear.trigger("click");
    expect(wrapper.emitted("keyPress")?.[0]).toEqual(["clear"]);
  });

  it("emits keyPress for save", () => {
    const wrapper = mountKB();
    const save = wrapper.findAll(".kb-key").at(15)!;
    save.trigger("click");
    expect(wrapper.emitted("keyPress")?.[0]).toEqual(["save"]);
  });

  it("emits keyPress for decimal point", () => {
    const wrapper = mountKB();
    const dot = wrapper.findAll(".kb-key").at(13)!;
    dot.trigger("click");
    expect(wrapper.emitted("keyPress")?.[0]).toEqual(["."]);
  });

  it("has correct CSS classes for special keys", () => {
    const wrapper = mountKB();
    const keys = wrapper.findAll(".kb-key");
    expect(keys.at(3)!.classes()).toContain("kb-key-fn");
    expect(keys.at(7)!.classes()).toContain("kb-key-op");
    expect(keys.at(11)!.classes()).toContain("kb-key-op");
    expect(keys.at(15)!.classes()).toContain("kb-key-save");
    expect(keys.at(14)!.classes()).toContain("kb-key-zero");
  });
});
