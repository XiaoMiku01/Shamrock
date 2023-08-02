package moe.fuqiuluo.xposed.tools;

public class util {
    private static final int MINUTE = 60;
    private static final int HOUR = 60 * MINUTE;
    private static final int DAY = 24 * HOUR;
    private static final int WEEK = 7 * DAY;
    private static final int MONTH = 30 * WEEK;
    private static final int SEASON = 3 * MONTH;
    private static final int YEAR = 4 * SEASON;

    public static String secondsToDate(long sec) {
        StringBuilder builder = new StringBuilder();
        byte temp = 0;
        int year = (int) (sec / YEAR);
        if (year != 0) {
            builder.append(year);
            builder.append("年");
            temp++;
        }
        long tmp = sec % YEAR;
        int season = (int) ((tmp) / SEASON);
        if (season != 0) {
            builder.append(season);
            builder.append("季");
            temp++;
        }
        tmp = (tmp) % SEASON;
        int month = (int) ((tmp) / MONTH);
        if (month != 0) {
            builder.append(month);
            builder.append("月");
            temp++;
        }
        if (temp == 3) return builder.toString();
        tmp = tmp % MONTH;
        int week = (int) ((tmp) / WEEK);
        if (week != 0) {
            builder.append(week);
            builder.append("周");
            temp++;
        }
        if (temp == 3) return builder.toString();
        tmp = tmp % WEEK;
        int day = (int) ((tmp) / DAY);
        if (day != 0) {
            builder.append(day);
            builder.append("日");
            temp++;
        }
        if (temp == 3) return builder.toString();
        tmp = tmp % DAY;
        int hour = (int) ((tmp) / HOUR);
        if (hour != 0) {
            builder.append(hour);
            builder.append("时");
            temp++;
        }
        if (temp == 3) return builder.toString();
        tmp = tmp % HOUR;
        int minute = (int) (tmp / MINUTE);
        if (minute != 0) {
            builder.append(minute);
            builder.append("分");
            temp++;
        }
        if (temp == 3) return builder.toString();
        sec = (int) (tmp % MINUTE);
        if (sec != 0) {
            builder.append(sec);
            builder.append("秒");
        }
        return builder.toString();
    }

    public static String buf_to_string(byte[] bArr) {
        StringBuilder str = new StringBuilder();
        if (bArr == null) {
            return "";
        }
        for (byte b : bArr) {
            str = new StringBuilder((str + Integer.toHexString((b >> 4) & 15)) + Integer.toHexString(b & 15));
        }
        return str.toString();
    }

    public static byte[] string_to_buf(String str) {
        if (str == null) {
            return new byte[0];
        }
        byte[] bArr = new byte[str.length() / 2];
        for (int i2 = 0; i2 < str.length() / 2; i2++) {
            int i3 = i2 * 2;
            bArr[i2] = (byte) ((get_char((byte) str.charAt(i3)) << 4) + get_char((byte) str.charAt(i3 + 1)));
        }
        return bArr;
    }

    public static byte get_char(byte b2) {
        int i2;
        if (b2 < 48 || b2 > 57) {
            byte b3 = 97;
            if (b2 < 97 || b2 > 102) {
                b3 = 65;
                if (b2 < 65 || b2 > 70) {
                    return (byte) 0;
                }
            }
            i2 = (b2 - b3) + 10;
        } else {
            i2 = b2 - 48;
        }
        return (byte) i2;
    }
}
