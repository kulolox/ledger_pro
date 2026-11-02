import { describe, it, expect } from 'vitest';
import {
  hexToRgb,
  rgbToHex,
  rgbToHsl,
  hslToRgb,
  hslToHex,
  hexToHsl,
  isValidHex,
} from '../color';

describe('color utils', () => {
  it('should parse 3-digit and 6-digit hex to rgb', () => {
    expect(hexToRgb('#fff')).toEqual({ r: 255, g: 255, b: 255 });
    expect(hexToRgb('#000000')).toEqual({ r: 0, g: 0, b: 0 });
    expect(hexToRgb('#FF0000')).toEqual({ r: 255, g: 0, b: 0 });
    expect(hexToRgb('invalid')).toBeNull();
  });

  it('should convert rgb to uppercase hex', () => {
    expect(rgbToHex(255, 0, 0)).toBe('#FF0000');
    expect(rgbToHex(0, 255, 0)).toBe('#00FF00');
    expect(rgbToHex(0, 0, 255)).toBe('#0000FF');
    expect(rgbToHex(255, 255, 255)).toBe('#FFFFFF');
  });

  it('should convert between HSL and HEX accurately', () => {
    // Red: 0°, 100%, 50% -> #FF0000
    expect(hslToHex(0, 100, 50)).toBe('#FF0000');
    // Green: 120°, 100%, 50% -> #00FF00
    expect(hslToHex(120, 100, 50)).toBe('#00FF00');
    // Blue: 240°, 100%, 50% -> #0000FF
    expect(hslToHex(240, 100, 50)).toBe('#0000FF');

    const redHsl = hexToHsl('#FF0000');
    expect(redHsl.h).toBe(0);
    expect(redHsl.s).toBe(100);
    expect(redHsl.l).toBe(50);
  });

  it('should validate hex strings', () => {
    expect(isValidHex('#fff')).toBe(true);
    expect(isValidHex('#FFFFFF')).toBe(true);
    expect(isValidHex('#123456')).toBe(true);
    expect(isValidHex('#12345')).toBe(false);
    expect(isValidHex('123456')).toBe(false);
    expect(isValidHex('#GGG123')).toBe(false);
  });
});
