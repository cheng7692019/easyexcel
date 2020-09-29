package com.gaoice.easyexcel.test;

import com.gaoice.easyexcel.ExcelBuilder;
import com.gaoice.easyexcel.SheetInfo;
import com.gaoice.easyexcel.data.Converter;
import com.gaoice.easyexcel.data.Counter;
import com.gaoice.easyexcel.data.DefaultHandlers;
import com.gaoice.easyexcel.test.entity.Grade;
import com.gaoice.easyexcel.test.entity.SexCountResult;
import com.gaoice.easyexcel.test.entity.Student;
import com.gaoice.easyexcel.test.style.MySheetStyle;
import org.junit.Before;
import org.junit.Test;

import java.io.FileOutputStream;
import java.util.*;

public class ExcelBuilderTests {

    List<Student> studentList;

    /**
     * 生成测试数据 studentList
     */
    @Before
    public void initList() {
        studentList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            studentList.add(genStudent(i));
        }
    }

    /**
     * 最简单的使用
     */
    @Test
    public void simple() throws Exception {
        /*
         * 定义sheetInfo
         */
        String sheetName = "学生信息表";
        String[] columnNames = {"姓名", "身份证号", "性别", "出生日期", "语文分数", "数学分数", "英语分数"};
        String[] classFieldNames = {"name", "cardId", "sex", "birthday", "grade.chineseGrade", "grade.mathGrade", "grade.englishGrade"};
        SheetInfo sheetInfo = new SheetInfo(sheetName, columnNames, classFieldNames, studentList);
        /*
         * 直接写入文件
         */
        FileOutputStream file = new FileOutputStream("simple.xlsx");
        ExcelBuilder.writeOutputStream(sheetInfo, file);
        file.close();
    }

    /**
     * 在simple的基础上
     * 添加标题 title
     * 使用Converter，对性别进行转换，对日期进行格式化
     */
    @Test
    public void simpleConverter() throws Exception {
        String sheetName = "学生信息表";
        String title = "学生信息表";
        String[] columnNames = {"姓名", "身份证号", "性别", "出生日期", "语文分数", "数学分数", "英语分数"};
        String[] classFieldNames = {"name", "cardId", "sex", "birthday", "grade.chineseGrade", "grade.mathGrade", "grade.englishGrade"};
        SheetInfo sheetInfo = new SheetInfo(sheetName, title, columnNames, classFieldNames, studentList);
        /*
         * 转换器
         */
        //性别转换可以使用Map，Map适合数据简单的映射，Converter适合对值进行复杂操作
        Map<Integer, String> sexMap = new HashMap<>();
        sexMap.put(1, "男生");
        sexMap.put(0, "女生");
        sheetInfo.putConverter("sex", sexMap);
        //日期格式转换为 yyyy-MM-dd
        sheetInfo.putConverter("birthday", DefaultHandlers.dateConverter);

        FileOutputStream file = new FileOutputStream("simpleConverter.xlsx");
        ExcelBuilder.writeOutputStream(sheetInfo, file);
        file.close();
    }

    /**
     * 在simpleConverter的基础上
     * 使用#开头的虚拟字段定义 序号列 总分数列，并为虚拟字段添加Converter
     */
    @Test
    public void simpleConverterVirtual() throws Exception {
        String sheetName = "学生信息表";
        String title = "学生信息表";
        String[] columnNames = {"序号", "姓名", "身份证号", "性别", "出生日期", "语文分数", "数学分数", "英语分数", "总分数"};
        String[] classFieldNames = {"#order", "name", "cardId", "sex", "birthday", "grade.chineseGrade", "grade.mathGrade", "grade.englishGrade", "#countGrade"};
        SheetInfo sheetInfo = new SheetInfo(sheetName, title, columnNames, classFieldNames, studentList);
        /*
         * 转换器
         */
        sheetInfo.putConverter("sex", sexConverter)
                .putConverter("birthday", DefaultHandlers.dateConverter);
        //虚拟字段 #order，通过转换器生成序号
        sheetInfo.putConverter("#order", DefaultHandlers.orderConverter);
        //虚拟字段 #countGrade，通过转换器生成总分数
        sheetInfo.putConverter("#countGrade", countGradeConverter);

        FileOutputStream file = new FileOutputStream("simpleConverterVirtual.xlsx");
        ExcelBuilder.writeOutputStream(sheetInfo, file);
        file.close();
    }

    /**
     * 在simpleConverterVirtual的基础上
     * 使用Counter，对 性别列 进行统计
     */
    @Test
    public void simpleConverterVirtualCounter() throws Exception {
        String sheetName = "计算机学院";
        String title = "学生信息表";
        String[] columnNames = {"序号", "姓名", "身份证号", "性别", "出生日期", "语文分数", "数学分数", "英语分数", "总分数"};
        String[] classFieldNames = {"#order", "name", "cardId", "sex", "birthday", "grade.chineseGrade", "grade.mathGrade", "grade.englishGrade", "#countGrade"};
        SheetInfo sheetInfo = new SheetInfo(sheetName, title, columnNames, classFieldNames, studentList);
        /*
         * 转换器
         */
        sheetInfo.putConverter("sex", sexConverter)
                .putConverter("birthday", DefaultHandlers.dateConverter)
                .putConverter("#order", DefaultHandlers.orderConverter)
                .putConverter("#countGrade", countGradeConverter);
        /*
         * 统计器
         */
        //序号列，统计结果显示“合计”字样
        sheetInfo.putCounter("#order", DefaultHandlers.orderCounter);
        //统计男女人数
        sheetInfo.putCounter("sex", sexCounter);

        FileOutputStream file = new FileOutputStream("simpleConverterVirtualCounter.xlsx");
        ExcelBuilder.writeOutputStream(sheetInfo, file);
        file.close();
    }

    /**
     * 在simpleConverterVirtualCounter基础上
     * 设置自定义style，对60分以下的分数标红，纯数字列自适应会遮挡，对身份证长度进行调整
     */
    @Test
    public void sheetStyle() throws Exception {
        String sheetName = "学生信息表";
        String title = "学生信息表";
        String[] columnNames = {"序号", "姓名", "身份证号", "性别", "出生日期", "语文分数", "数学分数", "英语分数", "总分数"};
        String[] classFieldNames = {"#order", "name", "cardId", "sex", "birthday", "grade.chineseGrade", "grade.mathGrade", "grade.englishGrade", "#countGrade"};
        SheetInfo sheetInfo = new SheetInfo(sheetName, title, columnNames, classFieldNames, studentList);
        /*
         * 转换器 统计器
         */
        sheetInfo.putConverter("sex", sexConverter)
                .putConverter("birthday", DefaultHandlers.dateConverter)
                .putConverter("#order", DefaultHandlers.orderConverter)
                .putConverter("#countGrade", countGradeConverter)
                .putCounter("#order", DefaultHandlers.orderCounter)
                .putCounter("sex", sexCounter);
        /*
         * 自定义样式，对分数列小于60分的分数设置为红色字体
         */
        sheetInfo.setSheetStyle(new MySheetStyle(sheetInfo));

        FileOutputStream file = new FileOutputStream("sheetStyle.xlsx");
        ExcelBuilder.writeOutputStream(sheetInfo, file);
        file.close();
    }

    /**
     * 两个sheet
     */
    @Test
    public void twoSheet() throws Exception {
        String sheetName1 = "学生信息表";
        String title1 = "学生信息表";
        String[] columnNames1 = {"序号", "姓名", "身份证号", "性别", "出生日期"};
        String[] classFieldNames1 = {"#order", "name", "cardId", "sex", "birthday"};
        SheetInfo sheetInfo1 = new SheetInfo(sheetName1, title1, columnNames1, classFieldNames1, studentList);
        sheetInfo1.putConverter("#order", DefaultHandlers.orderConverter)
                .putConverter("sex", sexConverter)
                .putConverter("birthday", DefaultHandlers.dateConverter)
                .putCounter("#order", DefaultHandlers.orderCounter)
                .putCounter("sex", sexCounter)
                .setSheetStyle(new MySheetStyle(sheetInfo1));

        String sheetName2 = "学生分数表";
        String title2 = "学生分数表";
        String[] columnNames2 = {"序号", "姓名", "语文分数", "数学分数", "英语分数", "总分数"};
        String[] classFieldNames2 = {"#order", "name", "grade.chineseGrade", "grade.mathGrade", "grade.englishGrade", "#countGrade"};
        SheetInfo sheetInfo2 = new SheetInfo(sheetName2, title2, columnNames2, classFieldNames2, studentList);
        sheetInfo2.putConverter("#order", DefaultHandlers.orderConverter)
                .putConverter("#countGrade", countGradeConverter)
                .setSheetStyle(new MySheetStyle(sheetInfo2));

        List<SheetInfo> sheetInfos = new ArrayList<>();
        sheetInfos.add(sheetInfo1);
        sheetInfos.add(sheetInfo2);

        FileOutputStream file = new FileOutputStream("twoSheet.xlsx");
        ExcelBuilder.writeOutputStream(sheetInfos, file);
        file.close();
    }

    /**
     * 性别Converter
     * value是Converter所属字段所对应的值
     * listIndex是value所属对象所在list的index
     * columnIndex是当前字段名所在classFieldNames的index
     */
    static Converter<Object, Object> sexConverter = (SheetInfo sheetInfo, Object value, int listIndex, int columnIndex) -> {
        /*
         *基本类型换被转换为对应的包装类型，这里int对应的是Integer，使用equals判断相等
         */
        if (value.equals(1)) {
            return "男生";
        } else {
            return "女生";
        }
    };
    /**
     * 总分数Converter
     */
    static Converter<Object, Object> countGradeConverter = (SheetInfo sheetInfo, Object value, int listIndex, int columnIndex) -> {
        /*
         * 虚拟字段传入的value是当前完整对象，在本例中是student对象
         */
        if (value instanceof Student) {
            Student student = (Student) value;
            Grade grade = student.getGrade();
            if (grade != null) {
                return grade.getChineseGrade() + grade.getMathGrade() + grade.getEnglishGrade();
            }
        }
        return null;
    };
    /**
     * 性别统计 Counter
     * result 保存了当前的合计结果，使用自定义类型要注意重写toString方法，以便在统计行显示自己想要的结果
     */
    static Counter<Object, Object> sexCounter = (SheetInfo sheetInfo, Object value, int listIndex, int columnIndex, Object result) -> {
        if (result == null) {
            result = new SexCountResult();
        }
        if (result instanceof SexCountResult) {
            SexCountResult sexCountResult = (SexCountResult) result;
            if (value.equals(1)) {
                sexCountResult.addManNum();
            } else {
                sexCountResult.addWomanNum();
            }
        }
        return result;
    };

    String[] chinese = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖", "拾"};

    private Student genStudent(int i) {
        Student student = new Student();
        student.setName("张" + chinese[i % 11]);
        student.setCardId("123123123412341234");
        student.setSex(i % 2);
        student.setBirthday(new Date());
        Grade grade = new Grade();
        grade.setChineseGrade(i % 10 * 10 + i % 10);
        grade.setMathGrade(i % 10 * 10 + i % 10);
        grade.setEnglishGrade(i % 10 * 10 + i % 10);
        student.setGrade(grade);
        return student;
    }
}
