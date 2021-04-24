/**
 * -*- coding: utf-8 -*-
 *
 * @Time : 2021/4/12 9:52
 * @Author : NekoSilverfox
 * @FileName: JsonManager
 * @Software: IntelliJ IDEA
 * @Versions: v0.1
 * @Github ：https://github.com/NekoSilverFox
 */
package com.example.utils;

import com.example.pojo.Ship;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Stream;


public class ReadWriteShips {

    /**
     * 将存储船舶的 ArrayList 写入到 json 文件中
     * @param ships 将存储船舶的 ArrayList
     * @param filePath 写入的路径
     * @throws IOException 文件无法创建或路径错误
     */
    public static void jsonWriter(ArrayList<Ship> ships, String filePath) throws IOException {
        File file = new File(filePath);
        checkBeforeWrite(filePath, file);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new FileWriter(filePath), ships);
    }

    private static void checkBeforeWrite(String filePath, File file) throws IOException {
        if (filePath.isEmpty() || !filePath.toLowerCase(Locale.ROOT).endsWith(".json")) {
            throw new FileNotFoundException("[ERROR] File path is empty or not a Json file");
        }else if (!file.exists()) {
            // 如果文件不存在就创建一个
            String[] split = filePath.split("\\\\");
            long count = Stream.of(split).count();

            if (count == 1) {
                file.createNewFile();
            } else {
                StringBuffer dirPath = new StringBuffer();
                for (int i = 0; i < count - 1; i++) {
                    dirPath.append(split[i]).append("\\\\");
                }

                new File(dirPath.toString()).mkdirs();
                file.createNewFile();
            }
        }
    }

    /**
     * 从 json 文件中，读取存储船舶的 ArrayList
     * @param filePath 读取的路径
     * @return 读取到的 ArrayList
     * @throws IOException 文件打开
     */
    public static ArrayList<Ship> jsonReader(String filePath) throws IOException {
        FileReader fileReader = new FileReader(filePath);
        int len = 0;
        char[] chars = new char[1024];
        StringBuffer stringBuffer = new StringBuffer();

        while ((len = fileReader.read(chars)) != -1) {
            stringBuffer.append(new String(chars, 0, len));
        }

        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Ship> freightersList = mapper.readValue(stringBuffer.toString(), new TypeReference<ArrayList<Ship>>() {});

        return freightersList;
    }
}
