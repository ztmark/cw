package com.mark;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Author: Mark
 * Date  : 16/2/22.
 */
public class CW {

    private static final String API = "http://wthrcdn.etouch.cn/weather_mini?city=";
    private static final int DAYS = 3;
    private static final String HEADER = "      今天             明天             后天       ";
    private static final String TEMPERATURE = "     %s            %s           %s       ";
    private static final String DESC = "      %s             %s             %s       ";

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 1) {
            usage();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(API + args[0]).build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String,Object> body = mapper.readValue(response.body().byteStream(), Map.class);
            if (body.get("desc").equals("OK")) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                System.out.println(buildInterface(data));
            } else {
                System.out.println("网络出错.....");
            }
        }


    }

    @SuppressWarnings("unchecked")
    private static String buildInterface(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("当前温度: ").append(data.get("wendu")).append("\n");
        List<Map<String, String>> forecast = (List<Map<String, String>>) data.get("forecast");
        String[] temp = new String[DAYS];
        WeatherType[] icon = new WeatherType[DAYS];
        String[] desc = new String[DAYS];
        int cnt = 0;
        for (Map<String, String> we : forecast) {
            if (cnt >= DAYS) {
                break;
            }
            temp[cnt] = we.get("low").split(" ")[1] + "~" + we.get("high").split(" ")[1];
            String type = we.get("type");
            icon[cnt] = WeatherType.value(type);
            desc[cnt] = type;
            cnt++;
        }
        sb.append(HEADER).append("\n");
        sb.append(String.format(TEMPERATURE, temp)).append("\n");
        sb.append(String.format(DESC, desc)).append("\n");
        sb.append(buildICON(icon));
        return sb.toString();
    }


    private static String buildICON(WeatherType[] types) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            for (WeatherType type : types) {
                sb.append(Weather.getWeather(type)[i]);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static class Weather {
        static String[] rain = {"      .-.        ",
                                "     (   ).      ",
                                "    (___(__)     ",
                                "    ‘ ‘ ‘ ‘      ",
                                "   ‘ ‘ ‘ ‘       "};
        static String[] sunny = {"    \\   /        ",
                                 "     .-.         ",
                                 "  ― (   ) ―      ",
                                 "     `-’         ",
                                 "    /   \\        "};
        static String[] cloudy = {"                 ",
                                   "     .--.        ",
                                   "  .-(    ).      ",
                                   " (___.__)__)     ",
                                   "                 "};
        static String[] snow = {"      .-.        ",
                                "     (   ).      ",
                                "    (___(__)     ",
                                "   *  *  * *     ",
                                "  *  *  * *    "};

        public static String[] getWeather(WeatherType type) {
            switch (type) {
                case SUNNY: return sunny;
                case RAIN: return rain;
                case CLOUDY: return cloudy;
                case SNOW: return snow;
                default: return cloudy;
            }
        }
    }

    private enum WeatherType {
        SUNNY, RAIN, CLOUDY, SNOW;

        public static WeatherType value(String type) {
            if (type.contains("晴")) {
                return WeatherType.SUNNY;
            } else if (type.contains("阴") || type.contains("多云")) {
                return WeatherType.CLOUDY;
            } else if (type.contains("雨")) {
                return WeatherType.RAIN;
            } else if (type.contains("雪")) {
                return WeatherType.SNOW;
            } else {
                return WeatherType.CLOUDY;
            }
        }
    }


    private static void usage() {
        System.out.println("Usage: CW <cityName>");
    }

}
