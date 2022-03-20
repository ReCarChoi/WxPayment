package com.recarchoi;

import java.util.*;

/**
 * @author recarchoi
 * @since 2022/3/19 22:33
 */
public class Solution {

    public static void main(String[] args) {
        //countHillValley(new int[]{57, 57, 57, 57, 57, 90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 85, 85, 85, 86, 86, 86});
        int llrr = countCollisions("SRRLRLRSRLRSSRRLSLRLLRSLSLLSSRRLSRSLSLRRS");
        System.out.println(llrr);
    }

    public static int countHillValley(int[] nums) {
        int count = 0;
        int rightTemp = 1;
        for (int i = 1; i < nums.length - 1; ++i) {
            if (nums[i] == nums[i - 1]) {
                continue;
            }
            while (i + rightTemp < nums.length - 1 && nums[i] == nums[i + rightTemp]) {
                ++rightTemp;
            }
            if (nums[i] > nums[i - 1] && nums[i] > nums[i + rightTemp]) {
                ++count;
                rightTemp = 1;
            }
            if (nums[i] < nums[i - 1] && nums[i] < nums[i + rightTemp]) {
                ++count;
                rightTemp = 1;
            }
            rightTemp = 1;
        }
        return count;
    }

    public static int countCollisions(String directions) {
        int collisionCount = 0;
        String[] split = directions.split("");
        for (int i = 0; i < split.length - 1; i++) {
            int temp = i;
            if ("R".equals(split[i])) {
                if ("S".equals(split[i + 1])) {
                    collisionCount += 1;
                    split[i] = "S";
                    split[i + 1] = "S";
                }
                if ("L".equals(split[i + 1])) {
                    collisionCount += 2;
                    split[i] = "S";
                    split[i + 1] = "S";
                }
            }
            if ("S".equals(split[i])) {
                if ("L".equals(split[i + 1])) {
                    collisionCount += 1;
                    split[i + 1] = "S";
                }
                while (temp > 0 && "R".equals(split[temp - 1])) {
                    collisionCount += 1;
                    split[temp - 1] = "S";
                    temp--;
                }
            }
        }
        return collisionCount;
    }

}
