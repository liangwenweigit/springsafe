package com.fly.validation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

/**
 * @author liang
 * @date 2019/1/8 - 14:13
 * 生成校验码的controller
 */
@Controller
@RequestMapping("/images")//images/**路径设置了不拦截的
public class GetCheckCodeController {

    //定义常量表示输出图片的高度宽度常量
    public static final int WIDTH = 120;
    public static final int HEIGHT = 25;
    private String[] fontNames  = {"宋体", "黑体", "微软雅黑", "谐体_GB2312"};//随机获取字体
    private Random r = new Random();

    @RequestMapping("/getCheckCode")
    public void getCheckCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //java写图片出客户端要用到bufferedImage //按照制定高度和宽度构建一个类型为rgb模式的图片
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_BGR);
        //获取画笔工具
        Graphics g = image.getGraphics();
        setBackgrond(g);//绘制背景图片
        setBorder(g);//绘制边框
        drawRandomLine(g);//绘制干扰线
        String code = drawRandomNum(g);//生成随机数并把这个随机数返回，同时把这个随机数写入图片中
        request.getSession().setAttribute("checkcode", code);//把随机数放置session对象中

        //最后把内存中的图片写出到客户端的浏览器上
        response.setContentType("image/jpeg");//写出客户端的是一个图片
        ImageIO.write(image, "jpg", response.getOutputStream());//三个参数 1要写出去的对象 2图片格式名称 3用那个管道写

    }

    private String drawRandomNum(Graphics g) {

        String code = "";
        String baseString = "23456789abcdefghjkmnopqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ";
        int x = r.nextInt(10)+10;
        int y = r.nextInt(5)+20;//基线坐标
        for (int i = 0; i < 4; i++) {
            g.setColor(randomColor());
            g.setFont(randomFont());//绘制字符串字体.样式.大小
            String base = baseString.charAt(r.nextInt(baseString.length()))+"";
            g.drawString(base, x, y);
            x = x + r.nextInt(20)+15;
            y = r.nextInt(5)+20;
            code += base;
        }
        return code;
    }
    private void drawRandomLine(Graphics g) {

        for (int i = 0; i < 12; i++) {
            g.setColor(randomColor());
            //随机得到干扰线的起始坐标点
            int x1 = r.nextInt(WIDTH);
            int y1 = r.nextInt(HEIGHT);
            //的到终点坐标点
            int x2 = r.nextInt(WIDTH);
            int y2 = r.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }
    }
    private void setBorder(Graphics g) {
        g.setColor(Color.BLUE);
        g.drawRect(1, 1, WIDTH-2, HEIGHT-2);//绘制指定矩形的边框。
    }
    private void setBackgrond(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);// 填充指定的矩形。 0 0坐标开始 画到XX坐标
    }

    /**
     * 随机获得颜色
     * @return
     */
    private Color randomColor () {
        int red = r.nextInt(150);
        int green = r.nextInt(150);
        int blue = r.nextInt(150);
        return new Color(red, green, blue);
    }
    /**
     * 随机获得字体
     * @return
     */
    private Font randomFont () {
        int index = r.nextInt(fontNames.length);
        String fontName = fontNames[index];
        int style = r.nextInt(4);
        int size = r.nextInt(5) + 24;
        return new Font(fontName, style, size);
    }

}
