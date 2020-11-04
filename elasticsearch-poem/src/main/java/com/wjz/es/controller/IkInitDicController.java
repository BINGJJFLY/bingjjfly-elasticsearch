package com.wjz.es.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/ikInitDic")
public class IkInitDicController {

    @GetMapping("/list")
    public String list(HttpServletRequest request) {
        List<String> result = new ArrayList<>();
        try {
            URL url = IkInitDicController.class.getClassLoader().getResource("static/init.dic");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0) {
                        result.add(line);
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        request.setAttribute("words", result);
        return "/ikinitdic/list";
    }

    @GetMapping("/save")
    public String save() {
        String word = "花西子";
        try {
            URL url = IkInitDicController.class.getClassLoader().getResource("static/init.dic");
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(url.getFile(), true), StandardCharsets.UTF_8))) {
                writer.write("\r\n");
                writer.write(word.trim());
                writer.flush();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return "forward:/ikInitDic/list";
    }

    @GetMapping("/delete")
    public String delete() {
        String word = "奇葩说";
        StringBuilder content = new StringBuilder();
        try {
            URL url = IkInitDicController.class.getClassLoader().getResource("static/init.dic");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0 && !line.equals(word)) {
                        content.append(line).append("\r\n");
                    }
                }
            }
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(url.getFile()), StandardCharsets.UTF_8))) {
                writer.write(content.toString());
                writer.flush();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return "forward:/ikInitDic/list";
    }

}
