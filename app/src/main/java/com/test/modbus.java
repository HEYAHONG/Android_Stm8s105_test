package com.test;

public class modbus {
    static byte [] buff=new byte[8];
    public static int CRC16(byte[] buf, int len) {
        int crc = 0xFFFF;
        byte i, j;
        for (j = 0; j < len; j++) {
            crc = crc ^ buf[j];
            for (i = 0; i < 8; i++) {
                if ((crc & 0x0001) > 0) {
                    crc = crc >> 1;
                    crc = crc ^ 0xa001;
                } else
                    crc = crc >> 1;
            }
        }
        return crc;
    }
    public static byte [] modbus_read(int salve_addr,int address)
    {
        //填写从机地址
        buff[0]=(byte)salve_addr;
        //填写功能代码
        buff[1]=0x03;
        // 填写寄存器地址
        buff[2]=(byte)((address & 0xff00) >>8);
        buff[3]=(byte)(address&0xff);
        //填写数量
        buff[4]=0x00;
        buff[5]=0x01;
        //填写校验（低字节在前）
        buff[7]=(byte)((CRC16(buff,6) & 0xff00) >>8);
        buff[6]=(byte)(CRC16(buff,6)&0xff);
       return buff;
    }
    public static byte [] modbus_write(int salve_addr,int address,byte data)
    {
        //填写从机地址
        buff[0]=(byte)salve_addr;
        //填写功能代码
        buff[1]=0x06;
        // 填写寄存器地址
        buff[2]=(byte)((address & 0xff00) >>8);
        buff[3]=(byte)(address&0xff);
        //填写数量
        buff[4]=0x00;
        buff[5]=data;
        //填写校验（低字节在前）
        buff[7]=(byte)((CRC16(buff,6) & 0xff00) >>8);
        buff[6]=(byte)(CRC16(buff,6)&0xff);

        return buff;
    }
}
