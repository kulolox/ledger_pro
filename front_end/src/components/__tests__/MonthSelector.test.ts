import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";
import MonthSelector from "../MonthSelector.vue";

function mountMonthSelector(props: any = { year: 2026, month: 6 }) {
  return mount(MonthSelector, {
    props,
    global: {
      components: {
        view: {
          template:
            '<div :class="$attrs.class" :style="$attrs.style" @click="$emit(\'click\', $event)"><slot /></div>',
          inheritAttrs: true,
        },
        text: {
          template: '<span :class="$attrs.class"><slot /></span>',
          inheritAttrs: true,
        },
        "wd-icon": {
          template: '<i :class="$attrs.name"></i>',
          inheritAttrs: true,
        },
        "wd-popup": {
          props: ["modelValue"],
          template: '<div v-if="modelValue" class="mock-popup"><slot /></div>',
        },
      },
    },
  });
}

describe("MonthSelector", () => {
  it("renders formatted year and month label without left/right nav arrows", () => {
    const wrapper = mountMonthSelector({ year: 2026, month: 6 });
    expect(wrapper.find(".month-label").text()).toBe("2026-06");
    expect(wrapper.find(".nav-arrow").exists()).toBe(false);
  });

  it("renders year label when mode is year", () => {
    const wrapper = mountMonthSelector({ year: 2026, month: 6, mode: "year", allowYear: true });
    expect(wrapper.find(".month-label").text()).toBe("2026年");
  });

  it("opens year-month picker popup on clicking capsule and selects year/month", async () => {
    const wrapper = mountMonthSelector({ year: 2026, month: 6 });
    expect(wrapper.find(".mock-popup").exists()).toBe(false);

    await wrapper.find(".month-capsule").trigger("click");
    expect(wrapper.find(".mock-popup").exists()).toBe(true);
    expect(wrapper.find(".year-display").text()).toBe("2026年");

    // Step year back to 2025
    const stepBtns = wrapper.findAll(".year-step-btn");
    await stepBtns[0].trigger("click");
    expect(wrapper.find(".year-display").text()).toBe("2025年");

    // Click October (10th month cell)
    const monthCells = wrapper.findAll(".month-cell");
    expect(monthCells.length).toBe(12);
    await monthCells[9].trigger("click");

    expect(wrapper.emitted("update:year")?.[0]).toEqual([2025]);
    expect(wrapper.emitted("update:month")?.[0]).toEqual([10]);
    expect(wrapper.emitted("change")?.[0]).toEqual([{ year: 2025, month: 10, mode: "month" }]);
    expect(wrapper.find(".mock-popup").exists()).toBe(false);
  });

  it("supports quick year pill selection inside popup", async () => {
    const wrapper = mountMonthSelector({ year: 2026, month: 6 });
    await wrapper.find(".month-capsule").trigger("click");

    const pills = wrapper.findAll(".year-pill");
    expect(pills.length).toBe(5);
    await pills[0].trigger("click"); // 2024
    expect(wrapper.find(".year-display").text()).toBe("2024年");

    const monthCells = wrapper.findAll(".month-cell");
    await monthCells[0].trigger("click"); // 1月
    expect(wrapper.emitted("change")?.[0]).toEqual([{ year: 2024, month: 1, mode: "month" }]);
  });

  it("supports switching to year mode and selecting a year", async () => {
    const wrapper = mountMonthSelector({ year: 2026, month: 6, allowYear: true });
    await wrapper.find(".month-capsule").trigger("click");

    // 点击切换为按年查询
    const segPills = wrapper.findAll(".segmented-pill");
    expect(segPills.length).toBe(2);
    await segPills[1].trigger("click"); // 按年查询

    // 应该出现年份网格
    const yearCells = wrapper.findAll(".year-cell");
    expect(yearCells.length).toBeGreaterThan(0);

    // 点击某个年份
    await yearCells[0].trigger("click");
    expect(wrapper.emitted("update:mode")?.[0]).toEqual(["year"]);
    expect((wrapper.emitted("change")?.[0][0] as any).mode).toBe("year");
    expect(wrapper.find(".mock-popup").exists()).toBe(false);
  });
});
