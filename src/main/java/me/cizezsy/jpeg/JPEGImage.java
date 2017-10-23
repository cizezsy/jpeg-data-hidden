package me.cizezsy.jpeg;


import me.cizezsy.huffman.HuffmanTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JPEGImage {

    public static final int TAG_SOI = 0xffd8;
    public static final int TAG_APP0 = 0xffe0;
    public static final int TAG_APPN_MIN = 0xffe1;
    public static final int TAG_APPN_MAX = 0xffef;
    public static final int TAG_DQT = 0xffdb;
    public static final int TAG_S0F0 = 0xffc0;
    public static final int TAG_DHT = 0xffc4;
    public static final int TAG_DRI = 0xffdd;
    public static final int TAG_SOS = 0xffda;
    public static final int TAG_EOI = 0xffd9;
    public static final int TAG_IMAGE_DATA = 0;
    public static final int TAG_MIN = 0xff01;
    public static final int TAG_MAX = 0xfffe;


    private Tag soi;
    private APP0 app0;
    private List<Tag> appn = new ArrayList<>();
    private List<DQT> dqt = new ArrayList<>();
    private SOF0 sof0;
    private List<DHT> dht = new ArrayList<>();
    private DRI dri;
    private SOS sos;
    private ImageData imageData;
    private Tag eoi;
    private List<ColorComponent> colorComponents = new ArrayList<>();

    //tag that doesn't be process  store in there
    //also make sure their order identical with origin
    private List<Tag> allTags = new ArrayList<>();
    private int[] unsignedByteData;

    public JPEGImage() {

    }

    public ColorComponent getOrCreateColorComponent(int id) {
        Optional<ColorComponent> colorComponentInfoOption =
                this.colorComponents.stream().filter(c -> c.getColorId() == id).findAny();

        ColorComponent colorComponent = colorComponentInfoOption.orElse(new ColorComponent());
        colorComponent.setColorId(id);
        this.colorComponents.add(colorComponent);
        return colorComponent;
    }

    public void setUnsignedByteData(int[] unsignedByteData) {
        this.unsignedByteData = unsignedByteData;
    }

    public int[] getUnsignedByteData() {
        return unsignedByteData;
    }

    public Optional<HuffmanTable> getAc(int id) {
        for (DHT dht : this.dht) {
            for (HuffmanTable hf : dht.getHuffmanTables()) {
                if (hf.getType() == HuffmanTable.AC && hf.getId() == id)
                    return Optional.of(hf);
            }
        }
        return Optional.empty();
    }

    public Optional<HuffmanTable> getDc(int id) {
        for (DHT dht : this.dht) {
            for (HuffmanTable hf : dht.getHuffmanTables()) {
                if (hf.getType() == HuffmanTable.DC && hf.getId() == id)
                    return Optional.of(hf);
            }
        }
        return Optional.empty();
    }

    public Tag getSoi() {
        return soi;
    }

    public void setSoi(Tag soi) {
        this.soi = soi;
    }

    public APP0 getApp0() {
        return app0;
    }

    public void setApp0(APP0 app0) {
        this.app0 = app0;
    }

    public List<Tag> getAppn() {
        return appn;
    }

    public void setAppn(List<Tag> appn) {
        this.appn = appn;
    }

    public List<DQT> getDqt() {
        return dqt;
    }

    public void setDqt(List<DQT> dqt) {
        this.dqt = dqt;
    }

    public SOF0 getSof0() {
        return sof0;
    }

    public void setSof0(SOF0 sof0) {
        this.sof0 = sof0;
    }

    public List<DHT> getDht() {
        return dht;
    }

    public void setDht(List<DHT> dht) {
        this.dht = dht;
    }

    public DRI getDri() {
        return dri;
    }

    public void setDri(DRI dri) {
        this.dri = dri;
    }

    public SOS getSos() {
        return sos;
    }

    public void setSos(SOS sos) {
        this.sos = sos;
    }

    public ImageData getImageData() {
        return imageData;
    }

    public void setImageData(ImageData imageData) {
        this.imageData = imageData;
    }

    public Tag getEoi() {
        return eoi;
    }

    public void setEoi(Tag eoi) {
        this.eoi = eoi;
    }

    public List<ColorComponent> getColorComponents() {
        return colorComponents;
    }

    public void setColorComponents(List<ColorComponent> colorComponents) {
        this.colorComponents = colorComponents;
    }

    public List<Tag> getAllTags() {
        return allTags;
    }

    public void setAllTags(List<Tag> allTags) {
        this.allTags = allTags;
    }

    public static class Tag {
        protected int tag;
        protected int position;
        protected int length;
        protected int stuffing;

        public int getTag() {
            return tag;
        }

        public void setTag(int tag) {
            this.tag = tag;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public int getStuffing() {
            return stuffing;
        }

        public void setStuffing(int stuffing) {
            this.stuffing = stuffing;
        }
    }


    /**
     * APP0                              length  Application，应用程序保留标记0
     * 标记代码                            2     固定值0xFFE0
     * 包含9个具体字段：
     * ① 数据长度                         2     ①~⑨9个字段的总长 即不包括标记代码，但包括本字段
     * ② 标识符                           5     固定值0x4A46494600，即字符串“JFIF0”
     * ③ 版本号                           2     一般是0x0102，表示JFIF的版本号1.2 可能会有其他数值代表其他版本
     * ④ X和Y的密度单位                   1     只有三个值可 0：无单位；1：点数/英寸；2：点数/厘米
     * ⑤ X方向像素密度                    2     取值范围未知
     * ⑥ Y方向像素密度                    2     取值范围未知
     * ⑦ 缩略图水平像素数目               1     取值范围未知
     * ⑧ 缩略图垂直像素数目               1     取值范围未知
     * ⑨ 缩略图RGB位图                    3x    缩略图RGB位图数据
     */
    public static class APP0 extends Tag {
        private int densityUnit;
        private int xDensity;
        private int yDensity;

        public APP0(Tag tag) {
            this.tag = tag.tag;
            this.position = tag.position;
            this.stuffing = tag.stuffing;
            this.length = tag.length;
        }

        public int getDensityUnit() {
            return densityUnit;
        }

        public void setDensityUnit(int densityUnit) {
            this.densityUnit = densityUnit;
        }

        public int getxDensity() {
            return xDensity;
        }

        public void setxDensity(int xDensity) {
            this.xDensity = xDensity;
        }

        public int getyDensity() {
            return yDensity;
        }

        public void setyDensity(int yDensity) {
            this.yDensity = yDensity;
        }
    }

    /**
     * DQT                                                          Define Quantization Table，定义量化表
     * 标记代码                           2                         固定值0xFFDB
     * 包含9个具体字段：
     * ① 数据长度                        2                         字段①和多个字段②的总长度 即不包括标记代码，但包括本字段
     * ② 量化表
     * a)精度及量化表ID                   1                         高4位：精度，只有两个可选值 0：8位；1：16位 低4位：量化表ID，取值范围为0～3
     * b)表项                        (64×(精度+1))                 例如8位精度的量化表 其表项长度为64×（0+1）=64字节
     */
    public static class DQT extends Tag {
        private int id;
        private int precision;
        private int itemLength;

        public DQT(Tag tag) {
            this.tag = tag.tag;
            this.position = tag.position;
            this.stuffing = tag.stuffing;
            this.length = tag.length;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getPrecision() {
            return precision;
        }

        public void setPrecision(int precision) {
            this.precision = precision;
        }

        public int getItemLength() {
            return itemLength;
        }

        public void setItemLength(int itemLength) {
            this.itemLength = itemLength;
        }
    }

    /**
     * SOF0                                               Start of Frame，帧图像开始
     * u 标记代码                   2     固定值0xFFC0
     * u 包含9个具体字段：
     * ① 数据长度                  2     ①~⑥六个字段的总长度 即不包括标记代码，但包括本字段
     * ② 精度                      1     每个数据样本的位数 通常是8位，一般软件都不支持 12位和16位
     * ③ 图像高度                  2     图像高度（单位：像素），如果不支持 DNL 就必须 >0
     * ④ 图像宽度                  2     图像宽度（单位：像素），如果不支持 DNL 就必须 >0
     * ⑤ 颜色分量数                1     只有3个数值可选 1：灰度图；3：YCrCb或YIQ；4：CMYK  而JFIF中使用YCrCb，故这里颜色分量数恒为3
     * ⑥颜色分量信息                     颜色分量数×3（通常为9）
     * a)颜色分量ID                 1
     * b)水平/垂直采样因子          1     高4位：水平采样因子 低4位：垂直采样因子
     * c) 量化表                    1     当前分量使用的量化表的ID
     */
    public static class SOF0 extends Tag {
        private int precision;
        int width;
        int height;


        public SOF0(Tag tag) {
            this.tag = tag.tag;
            this.position = tag.position;
            this.stuffing = tag.stuffing;
            this.length = tag.length;
        }

        public int getPrecision() {
            return precision;
        }

        public void setPrecision(int precision) {
            this.precision = precision;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    public static class DHT extends Tag {
        private List<HuffmanTable> huffmanTables = new ArrayList<>();

        public DHT(Tag tag) {
            this.tag = tag.tag;
            this.position = tag.position;
            this.stuffing = tag.stuffing;
            this.length = tag.length;
        }

        public List<HuffmanTable> getHuffmanTables() {
            return huffmanTables;
        }

        public void setHuffmanTables(List<HuffmanTable> huffmanTables) {
            this.huffmanTables = huffmanTables;
        }

    }

    public static class DRI extends Tag {
        private int interval;

        public DRI(Tag tag) {
            this.tag = tag.tag;
            this.position = tag.position;
            this.stuffing = tag.stuffing;
            this.length = tag.length;
        }

        public int getInterval() {
            return interval;
        }

        public void setInterval(int interval) {
            this.interval = interval;
        }
    }

    public static class SOS extends Tag {

        public SOS(Tag tag) {
            this.tag = tag.tag;
            this.position = tag.position;
            this.stuffing = tag.stuffing;
            this.length = tag.length;
        }

    }

    public static class ImageData extends Tag {
        private int[] data;

        public ImageData(Tag tag) {
            this.tag = tag.tag;
            this.position = tag.position;
            this.stuffing = tag.stuffing;
            this.length = tag.length;
        }

        public int[] getData() {
            return data;
        }

        public void setData(int[] data) {
            this.data = data;
        }
    }


}