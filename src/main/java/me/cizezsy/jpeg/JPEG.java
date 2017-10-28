package me.cizezsy.jpeg;

public class JPEG {
    public final static int APP0 = 0xFFE0;   // JFIF APP0 segment tag
    public final static int APP1 = 0xFFE1;
    public final static int APP2 = 0xFFE2;
    public final static int APP3 = 0xFFE3;
    public final static int APP4 = 0xFFE4;
    public final static int APP5 = 0xFFE5;
    public final static int APP6 = 0xFFE6;
    public final static int APP7 = 0xFFE7;
    public final static int APP8 = 0xFFE8;
    public final static int APP9 = 0xFFE9;
    public final static int APP10 = 0xFFEA;
    public final static int APP11 = 0xFFEB;
    public final static int APP12 = 0xFFEC;
    public final static int APP13 = 0xFFED;
    public final static int APP14 = 0xFFEE;
    public final static int APP15 = 0xFFEF;
    public final static int COM = 0xFFFE;   // Comment
    public final static int DAC = 0xFFCC;   // Define Arithmetic Table, usually unsupported
    public final static int DHP = 0xFFDE;   // Reserved, fatal error
    public final static int DHT = 0xFFC4;   // Define Huffman Table
    public final static int DNL = 0xFFDC;
    public final static int DQT = 0xFFDB;   // Define Quantization Table
    public final static int DRI = 0xFFDD;   // Define Restart Interval
    public final static int EOI = 0xFFD9;   // End Of Image
    public final static int EXP = 0xFFDF;   // Reserved, fatal error
    public final static int JPG = 0xFFC8;   // Reserved, fatal error
    public final static int JPG0 = 0xFFF0;   // Reserved, fatal error
    public final static int JPG13 = 0xFFFD;   // Reserved, fatal error
    public final static int RST0 = 0xFFD0;   // RSTn are used for resync
    public final static int RST1 = 0xFFD1;
    public final static int RST2 = 0xFFD2;
    public final static int RST3 = 0xFFD3;
    public final static int RST4 = 0xFFD4;
    public final static int RST5 = 0xFFD5;
    public final static int RST6 = 0xFFD6;
    public final static int RST7 = 0xFFD7;
    public final static int SOF0 = 0xFFC0;   // Baseline
    public final static int SOF1 = 0xFFC1;   // Extended sequential, Huffman
    public final static int SOF2 = 0xFFC2;   // Progressive, Huffman
    public final static int SOF3 = 0xFFC3;   // Unsupported; Lossless, Huffman
    public final static int SOF5 = 0xFFC5;   // Unsupported; Differential sequential, Huffman
    public final static int SOF6 = 0xFFC6;   // Unsupported; Differential progressive, Huffman
    public final static int SOF7 = 0xFFC7;   // Unsupported; Differential lossless, Huffman
    public final static int SOF9 = 0xFFC9;   // Extended sequential, arithmetic
    public final static int SOF10 = 0xFFCA;   // Progressive, arithmetic
    public final static int SOF11 = 0xFFCB;   // Unsupported; Lossless, Unsupported; arithmetic
    public final static int SOF13 = 0xFFCD;   // Unsupported; Differential sequential, arithmetic
    public final static int SOF14 = 0xFFCE;   // Unsupported; Differential progressive, arithmetic
    public final static int SOF15 = 0xFFCF;   // Unsupported; Differential lossless, arithmetic
    public final static int SOI = 0xFFD8;   // Start Of Image
    public final static int SOS = 0xFFDA;   // Start Of Scan

    public final static int IMAGE = -1;

}
