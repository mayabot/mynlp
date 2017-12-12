/*
 *  Copyright 2017 mayabot.com authors. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.mayabot.nlp.segment.corpus.tag;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 词性.
 * 使用单例模式，代替枚举类型
 *
 * @author jimichan
 */
public class Nature {

    public final String name;
    public final int ord;

    private static Map<String, Nature> map = new ConcurrentHashMap<String, Nature>();
    private static Map<Integer, Nature> intmap = new ConcurrentHashMap<Integer, Nature>();

    private Nature(String name, int ord) {
        this.name = name;
        this.ord = ord;
        if (map.put(name, this) != null) throw new RuntimeException();
        if (intmap.put(ord, this) != null) throw new RuntimeException();
    }

    public Nature registe(String name, int ord) {
        return new Nature(name, ord);
    }

    public static final Nature[] values() {
        return map.values().toArray(new Nature[0]);
    }

    public static final Nature valueOf(int ord) {
        switch (ord) {
            case 128:
                return xx;
            case 40:
                return nic;
            case 113:
                return ude2;
            case 114:
                return ude3;
            case 89:
                return ryt;
            case 90:
                return rys;
            case 112:
                return ude1;
            case 11:
                return yg;
            case 91:
                return ryv;
            case 4:
                return qg;
            case 39:
                return nit;
            case 41:
                return nis;
            case 105:
                return pbei;
            case 16:
                return nrf;
            case 75:
                return ad;
            case 99:
                return qt;
            case 132:
                return wky;
            case 98:
                return qv;
            case 77:
                return ag;
            case 15:
                return nrj;
            case 131:
                return wkz;
            case 86:
                return rzs;
            case 78:
                return al;
            case 45:
                return nba;
            case 12:
                return zg;
            case 76:
                return an;
            case 85:
                return rzt;
            case 46:
                return nbc;
            case 87:
                return rzv;
            case 92:
                return rg;
            case 47:
                return nbp;
            case 147:
                return begin;
            case 20:
                return nsf;
            case 83:
                return rr;
            case 0:
                return bg;
            case 88:
                return ry;
            case 84:
                return rz;
            case 67:
                return vshi;
            case 80:
                return bl;
            case 120:
                return uzhi;
            case 117:
                return uyy;
            case 93:
                return Rg;
            case 109:
                return uzhe;
            case 22:
                return ntc;
            case 107:
                return cc;
            case 74:
                return a;
            case 79:
                return b;
            case 29:
                return nth;
            case 106:
                return c;
            case 100:
                return d;
            case 122:
                return e;
            case 63:
                return f;
            case 49:
                return g;
            case 26:
                return nto;
            case 125:
                return h;
            case 58:
                return i;
            case 57:
                return j;
            case 126:
                return k;
            case 28:
                return nts;
            case 59:
                return l;
            case 94:
                return m;
            case 27:
                return ntu;
            case 13:
                return n;
            case 124:
                return o;
            case 103:
                return p;
            case 116:
                return udeng;
            case 97:
                return q;
            case 82:
                return r;
            case 61:
                return tg;
            case 62:
                return s;
            case 60:
                return t;
            case 108:
                return u;
            case 64:
                return v;
            case 130:
                return w;
            case 127:
                return x;
            case 123:
                return y;
            case 81:
                return z;
            case 101:
                return dg;
            case 43:
                return nmc;
            case 102:
                return dl;
            case 115:
                return usuo;
            case 5:
                return ud;
            case 8:
                return ug;
            case 6:
                return uj;
            case 24:
                return ntcb;
            case 9:
                return ul;
            case 25:
                return ntch;
            case 1:
                return mg;
            case 23:
                return ntcf;
            case 111:
                return uguo;
            case 10:
                return uv;
            case 95:
                return mq;
            case 7:
                return uz;
            case 35:
                return nnd;
            case 65:
                return vd;
            case 104:
                return pba;
            case 110:
                return ule;
            case 69:
                return vf;
            case 73:
                return vg;
            case 71:
                return vi;
            case 44:
                return nb;
            case 34:
                return nnt;
            case 72:
                return vl;
            case 37:
                return nf;
            case 66:
                return vn;
            case 118:
                return udh;
            case 36:
                return ng;
            case 30:
                return nh;
            case 96:
                return Mg;
            case 38:
                return ni;
            case 119:
                return uls;
            case 2:
                return nl;
            case 42:
                return nm;
            case 33:
                return nn;
            case 70:
                return vx;
            case 14:
                return nr;
            case 19:
                return ns;
            case 21:
                return nt;
            case 3:
                return nx;
            case 144:
                return wb;
            case 48:
                return nz;
            case 138:
                return wd;
            case 139:
                return wf;
            case 145:
                return wh;
            case 135:
                return wj;
            case 134:
                return wyy;
            case 141:
                return wm;
            case 140:
                return wn;
            case 133:
                return wyz;
            case 143:
                return wp;
            case 146:
                return end;
            case 53:
                return gb;
            case 52:
                return gc;
            case 142:
                return ws;
            case 137:
                return wt;
            case 68:
                return vyou;
            case 55:
                return gg;
            case 136:
                return ww;
            case 56:
                return gi;
            case 50:
                return gm;
            case 54:
                return gbc;
            case 51:
                return gp;
            case 32:
                return nhd;
            case 17:
                return nr1;
            case 18:
                return nr2;
            case 31:
                return nhm;
            case 121:
                return ulian;
            case 129:
                return xu;
            default:
                return intmap.get(ord);
        }

    }

    public static final Nature valueOf(String name) {
        switch (name) {
            case "bg":
                return bg;
            case "mg":
                return mg;
            case "nl":
                return nl;
            case "nx":
                return nx;
            case "qg":
                return qg;
            case "ud":
                return ud;
            case "uj":
                return uj;
            case "uz":
                return uz;
            case "ug":
                return ug;
            case "ul":
                return ul;
            case "uv":
                return uv;
            case "yg":
                return yg;
            case "zg":
                return zg;
            case "n":
                return n;
            case "nr":
                return nr;
            case "nrj":
                return nrj;
            case "nrf":
                return nrf;
            case "nr1":
                return nr1;
            case "nr2":
                return nr2;
            case "ns":
                return ns;
            case "nsf":
                return nsf;
            case "nt":
                return nt;
            case "ntc":
                return ntc;
            case "ntcf":
                return ntcf;
            case "ntcb":
                return ntcb;
            case "ntch":
                return ntch;
            case "nto":
                return nto;
            case "ntu":
                return ntu;
            case "nts":
                return nts;
            case "nth":
                return nth;
            case "nh":
                return nh;
            case "nhm":
                return nhm;
            case "nhd":
                return nhd;
            case "nn":
                return nn;
            case "nnt":
                return nnt;
            case "nnd":
                return nnd;
            case "ng":
                return ng;
            case "nf":
                return nf;
            case "ni":
                return ni;
            case "nit":
                return nit;
            case "nic":
                return nic;
            case "nis":
                return nis;
            case "nm":
                return nm;
            case "nmc":
                return nmc;
            case "nb":
                return nb;
            case "nba":
                return nba;
            case "nbc":
                return nbc;
            case "nbp":
                return nbp;
            case "nz":
                return nz;
            case "g":
                return g;
            case "gm":
                return gm;
            case "gp":
                return gp;
            case "gc":
                return gc;
            case "gb":
                return gb;
            case "gbc":
                return gbc;
            case "gg":
                return gg;
            case "gi":
                return gi;
            case "j":
                return j;
            case "i":
                return i;
            case "l":
                return l;
            case "t":
                return t;
            case "tg":
                return tg;
            case "s":
                return s;
            case "f":
                return f;
            case "v":
                return v;
            case "vd":
                return vd;
            case "vn":
                return vn;
            case "vshi":
                return vshi;
            case "vyou":
                return vyou;
            case "vf":
                return vf;
            case "vx":
                return vx;
            case "vi":
                return vi;
            case "vl":
                return vl;
            case "vg":
                return vg;
            case "a":
                return a;
            case "ad":
                return ad;
            case "an":
                return an;
            case "ag":
                return ag;
            case "al":
                return al;
            case "b":
                return b;
            case "bl":
                return bl;
            case "z":
                return z;
            case "r":
                return r;
            case "rr":
                return rr;
            case "rz":
                return rz;
            case "rzt":
                return rzt;
            case "rzs":
                return rzs;
            case "rzv":
                return rzv;
            case "ry":
                return ry;
            case "ryt":
                return ryt;
            case "rys":
                return rys;
            case "ryv":
                return ryv;
            case "rg":
                return rg;
            case "Rg":
                return Rg;
            case "m":
                return m;
            case "mq":
                return mq;
            case "Mg":
                return Mg;
            case "q":
                return q;
            case "qv":
                return qv;
            case "qt":
                return qt;
            case "d":
                return d;
            case "dg":
                return dg;
            case "dl":
                return dl;
            case "p":
                return p;
            case "pba":
                return pba;
            case "pbei":
                return pbei;
            case "c":
                return c;
            case "cc":
                return cc;
            case "u":
                return u;
            case "uzhe":
                return uzhe;
            case "ule":
                return ule;
            case "uguo":
                return uguo;
            case "ude1":
                return ude1;
            case "ude2":
                return ude2;
            case "ude3":
                return ude3;
            case "usuo":
                return usuo;
            case "udeng":
                return udeng;
            case "uyy":
                return uyy;
            case "udh":
                return udh;
            case "uls":
                return uls;
            case "uzhi":
                return uzhi;
            case "ulian":
                return ulian;
            case "e":
                return e;
            case "y":
                return y;
            case "o":
                return o;
            case "h":
                return h;
            case "k":
                return k;
            case "x":
                return x;
            case "xx":
                return xx;
            case "xu":
                return xu;
            case "w":
                return w;
            case "wkz":
                return wkz;
            case "wky":
                return wky;
            case "wyz":
                return wyz;
            case "wyy":
                return wyy;
            case "wj":
                return wj;
            case "ww":
                return ww;
            case "wt":
                return wt;
            case "wd":
                return wd;
            case "wf":
                return wf;
            case "wn":
                return wn;
            case "wm":
                return wm;
            case "ws":
                return ws;
            case "wp":
                return wp;
            case "wb":
                return wb;
            case "wh":
                return wh;
            case "end":
                return end;
            case "begin":
                return begin;

            default:
                Nature _v = map.get(name);
                if (_v != null) {
                    return _v;
                }
                _v = new Nature(name, Hashing.murmur3_128().hashString(name, Charsets.UTF_8).asInt());
                map.put(name, _v);
                return _v;
        }

    }

    static {
        bg = new Nature("bg", 0);
        mg = new Nature("mg", 1);
        nl = new Nature("nl", 2);
        nx = new Nature("nx", 3);
        qg = new Nature("qg", 4);
        ud = new Nature("ud", 5);
        uj = new Nature("uj", 6);
        uz = new Nature("uz", 7);
        ug = new Nature("ug", 8);
        ul = new Nature("ul", 9);
        uv = new Nature("uv", 10);
        yg = new Nature("yg", 11);
        zg = new Nature("zg", 12);
        // 以上标签来自ICT，以下标签来自北大
        n = new Nature("n", 13);
        nr = new Nature("nr", 14);
        nrj = new Nature("nrj", 15);
        nrf = new Nature("nrf", 16);
        nr1 = new Nature("nr1", 17);
        nr2 = new Nature("nr2", 18);
        ns = new Nature("ns", 19);
        nsf = new Nature("nsf", 20);
        nt = new Nature("nt", 21);
        ntc = new Nature("ntc", 22);
        ntcf = new Nature("ntcf", 23);
        ntcb = new Nature("ntcb", 24);
        ntch = new Nature("ntch", 25);
        nto = new Nature("nto", 26);
        ntu = new Nature("ntu", 27);
        nts = new Nature("nts", 28);
        nth = new Nature("nth", 29);
        nh = new Nature("nh", 30);
        nhm = new Nature("nhm", 31);
        nhd = new Nature("nhd", 32);
        nn = new Nature("nn", 33);
        nnt = new Nature("nnt", 34);
        nnd = new Nature("nnd", 35);
        ng = new Nature("ng", 36);
        nf = new Nature("nf", 37);
        ni = new Nature("ni", 38);
        nit = new Nature("nit", 39);
        nic = new Nature("nic", 40);
        nis = new Nature("nis", 41);
        nm = new Nature("nm", 42);
        nmc = new Nature("nmc", 43);
        nb = new Nature("nb", 44);
        nba = new Nature("nba", 45);
        nbc = new Nature("nbc", 46);
        nbp = new Nature("nbp", 47);
        nz = new Nature("nz", 48);
        g = new Nature("g", 49);
        gm = new Nature("gm", 50);
        gp = new Nature("gp", 51);
        gc = new Nature("gc", 52);
        gb = new Nature("gb", 53);
        gbc = new Nature("gbc", 54);
        gg = new Nature("gg", 55);
        gi = new Nature("gi", 56);
        j = new Nature("j", 57);
        i = new Nature("i", 58);
        l = new Nature("l", 59);
        t = new Nature("t", 60);
        tg = new Nature("tg", 61);
        s = new Nature("s", 62);
        f = new Nature("f", 63);
        v = new Nature("v", 64);
        vd = new Nature("vd", 65);
        vn = new Nature("vn", 66);
        vshi = new Nature("vshi", 67);
        vyou = new Nature("vyou", 68);
        vf = new Nature("vf", 69);
        vx = new Nature("vx", 70);
        vi = new Nature("vi", 71);
        vl = new Nature("vl", 72);
        vg = new Nature("vg", 73);
        a = new Nature("a", 74);
        ad = new Nature("ad", 75);
        an = new Nature("an", 76);
        ag = new Nature("ag", 77);
        al = new Nature("al", 78);
        b = new Nature("b", 79);
        bl = new Nature("bl", 80);
        z = new Nature("z", 81);
        r = new Nature("r", 82);
        rr = new Nature("rr", 83);
        rz = new Nature("rz", 84);
        rzt = new Nature("rzt", 85);
        rzs = new Nature("rzs", 86);
        rzv = new Nature("rzv", 87);
        ry = new Nature("ry", 88);
        ryt = new Nature("ryt", 89);
        rys = new Nature("rys", 90);
        ryv = new Nature("ryv", 91);
        rg = new Nature("rg", 92);
        Rg = new Nature("Rg", 93);
        m = new Nature("m", 94);
        mq = new Nature("mq", 95);
        Mg = new Nature("Mg", 96);
        q = new Nature("q", 97);
        qv = new Nature("qv", 98);
        qt = new Nature("qt", 99);
        d = new Nature("d", 100);
        dg = new Nature("dg", 101);
        dl = new Nature("dl", 102);
        p = new Nature("p", 103);
        pba = new Nature("pba", 104);
        pbei = new Nature("pbei", 105);
        c = new Nature("c", 106);
        cc = new Nature("cc", 107);
        u = new Nature("u", 108);
        uzhe = new Nature("uzhe", 109);
        ule = new Nature("ule", 110);
        uguo = new Nature("uguo", 111);
        ude1 = new Nature("ude1", 112);
        ude2 = new Nature("ude2", 113);
        ude3 = new Nature("ude3", 114);
        usuo = new Nature("usuo", 115);
        udeng = new Nature("udeng", 116);
        uyy = new Nature("uyy", 117);
        udh = new Nature("udh", 118);
        uls = new Nature("uls", 119);
        uzhi = new Nature("uzhi", 120);
        ulian = new Nature("ulian", 121);
        e = new Nature("e", 122);
        y = new Nature("y", 123);
        o = new Nature("o", 124);
        h = new Nature("h", 125);
        k = new Nature("k", 126);
        x = new Nature("x", 127);
        xx = new Nature("xx", 128);
        xu = new Nature("xu", 129);
        w = new Nature("w", 130);
        wkz = new Nature("wkz", 131);
        wky = new Nature("wky", 132);
        wyz = new Nature("wyz", 133);
        wyy = new Nature("wyy", 134);
        wj = new Nature("wj", 135);
        ww = new Nature("ww", 136);
        wt = new Nature("wt", 137);
        wd = new Nature("wd", 138);
        wf = new Nature("wf", 139);
        wn = new Nature("wn", 140);
        wm = new Nature("wm", 141);
        ws = new Nature("ws", 142);
        wp = new Nature("wp", 143);
        wb = new Nature("wb", 144);
        wh = new Nature("wh", 145);
        end = new Nature("end", 146);
        begin = new Nature("begin", 147);

    }

    /**
     * 区别语素
     */
    public static final Nature bg;

    /**
     * 数语素
     */
    public static final Nature mg;

    /**
     * 名词性惯用语
     */
    public static final Nature nl;

    /**
     * 字母专名
     */
    public static final Nature nx;

    /**
     * 量词语素
     */
    public static final Nature qg;

    /**
     * 助词
     */
    public static final Nature ud;

    /**
     * 助词
     */
    public static final Nature uj;

    /**
     * 着
     */
    public static final Nature uz;

    /**
     * 过
     */
    public static final Nature ug;

    /**
     * 连词
     */
    public static final Nature ul;

    /**
     * 连词
     */
    public static final Nature uv;

    /**
     * 语气语素
     */
    public static final Nature yg;

    /**
     * 状态词
     */
    public static final Nature zg;

    // 以上标签来自ICT，以下标签来自北大

    /**
     * 名词
     */
    public static final Nature n;

    /**
     * 人名
     */
    public static final Nature nr;

    /**
     * 日语人名
     */
    public static final Nature nrj;

    /**
     * 音译人名
     */
    public static final Nature nrf;

    /**
     * 复姓
     */
    public static final Nature nr1;

    /**
     * 蒙古姓名
     */
    public static final Nature nr2;

    /**
     * 地名
     */
    public static final Nature ns;

    /**
     * 音译地名
     */
    public static final Nature nsf;

    /**
     * 机构团体名
     */
    public static final Nature nt;

    /**
     * 公司名
     */
    public static final Nature ntc;

    /**
     * 工厂
     */
    public static final Nature ntcf;

    /**
     * 银行
     */
    public static final Nature ntcb;

    /**
     * 酒店宾馆
     */
    public static final Nature ntch;

    /**
     * 政府机构
     */
    public static final Nature nto;

    /**
     * 大学
     */
    public static final Nature ntu;

    /**
     * 中小学
     */
    public static final Nature nts;

    /**
     * 医院
     */
    public static final Nature nth;

    /**
     * 医药疾病等健康相关名词
     */
    public static final Nature nh;

    /**
     * 药品
     */
    public static final Nature nhm;

    /**
     * 疾病
     */
    public static final Nature nhd;

    /**
     * 工作相关名词
     */
    public static final Nature nn;

    /**
     * 职务职称
     */
    public static final Nature nnt;

    /**
     * 职业
     */
    public static final Nature nnd;

    /**
     * 名词性语素
     */
    public static final Nature ng;

    /**
     * 食品，比如“薯片”
     */
    public static final Nature nf;

    /**
     * 机构相关（不是独立机构名）
     */
    public static final Nature ni;

    /**
     * 教育相关机构
     */
    public static final Nature nit;

    /**
     * 下属机构
     */
    public static final Nature nic;

    /**
     * 机构后缀
     */
    public static final Nature nis;

    /**
     * 物品名
     */
    public static final Nature nm;

    /**
     * 化学品名
     */
    public static final Nature nmc;

    /**
     * 生物名
     */
    public static final Nature nb;

    /**
     * 动物名
     */
    public static final Nature nba;

    /**
     * 动物纲目
     */
    public static final Nature nbc;

    /**
     * 植物名
     */
    public static final Nature nbp;

    /**
     * 其他专名
     */
    public static final Nature nz;

    /**
     * 学术词汇
     */
    public static final Nature g;

    /**
     * 数学相关词汇
     */
    public static final Nature gm;

    /**
     * 物理相关词汇
     */
    public static final Nature gp;

    /**
     * 化学相关词汇
     */
    public static final Nature gc;

    /**
     * 生物相关词汇
     */
    public static final Nature gb;

    /**
     * 生物类别
     */
    public static final Nature gbc;

    /**
     * 地理地质相关词汇
     */
    public static final Nature gg;

    /**
     * 计算机相关词汇
     */
    public static final Nature gi;

    /**
     * 简称略语
     */
    public static final Nature j;

    /**
     * 成语
     */
    public static final Nature i;

    /**
     * 习用语
     */
    public static final Nature l;

    /**
     * 时间词
     */
    public static final Nature t;

    /**
     * 时间词性语素
     */
    public static final Nature tg;

    /**
     * 处所词
     */
    public static final Nature s;

    /**
     * 方位词
     */
    public static final Nature f;

    /**
     * 动词
     */
    public static final Nature v;

    /**
     * 副动词
     */
    public static final Nature vd;

    /**
     * 名动词
     */
    public static final Nature vn;

    /**
     * 动词“是”
     */
    public static final Nature vshi;

    /**
     * 动词“有”
     */
    public static final Nature vyou;

    /**
     * 趋向动词
     */
    public static final Nature vf;

    /**
     * 形式动词
     */
    public static final Nature vx;

    /**
     * 不及物动词（内动词）
     */
    public static final Nature vi;

    /**
     * 动词性惯用语
     */
    public static final Nature vl;

    /**
     * 动词性语素
     */
    public static final Nature vg;

    /**
     * 形容词
     */
    public static final Nature a;

    /**
     * 副形词
     */
    public static final Nature ad;

    /**
     * 名形词
     */
    public static final Nature an;

    /**
     * 形容词性语素
     */
    public static final Nature ag;

    /**
     * 形容词性惯用语
     */
    public static final Nature al;

    /**
     * 区别词
     */
    public static final Nature b;

    /**
     * 区别词性惯用语
     */
    public static final Nature bl;

    /**
     * 状态词
     */
    public static final Nature z;

    /**
     * 代词
     */
    public static final Nature r;

    /**
     * 人称代词
     */
    public static final Nature rr;

    /**
     * 指示代词
     */
    public static final Nature rz;

    /**
     * 时间指示代词
     */
    public static final Nature rzt;

    /**
     * 处所指示代词
     */
    public static final Nature rzs;

    /**
     * 谓词性指示代词
     */
    public static final Nature rzv;

    /**
     * 疑问代词
     */
    public static final Nature ry;

    /**
     * 时间疑问代词
     */
    public static final Nature ryt;

    /**
     * 处所疑问代词
     */
    public static final Nature rys;

    /**
     * 谓词性疑问代词
     */
    public static final Nature ryv;

    /**
     * 代词性语素
     */
    public static final Nature rg;

    /**
     * 古汉语代词性语素
     */
    public static final Nature Rg;

    /**
     * 数词
     */
    public static final Nature m;

    /**
     * 数量词
     */
    public static final Nature mq;

    /**
     * 甲乙丙丁之类的数词
     */
    public static final Nature Mg;

    /**
     * 量词
     */
    public static final Nature q;

    /**
     * 动量词
     */
    public static final Nature qv;

    /**
     * 时量词
     */
    public static final Nature qt;

    /**
     * 副词
     */
    public static final Nature d;

    /**
     * 辄,俱,复之类的副词
     */
    public static final Nature dg;

    /**
     * 连语
     */
    public static final Nature dl;

    /**
     * 介词
     */
    public static final Nature p;

    /**
     * 介词“把”
     */
    public static final Nature pba;

    /**
     * 介词“被”
     */
    public static final Nature pbei;

    /**
     * 连词
     */
    public static final Nature c;

    /**
     * 并列连词
     */
    public static final Nature cc;

    /**
     * 助词
     */
    public static final Nature u;

    /**
     * 着
     */
    public static final Nature uzhe;

    /**
     * 了 喽
     */
    public static final Nature ule;

    /**
     * 过
     */
    public static final Nature uguo;

    /**
     * 的 底
     */
    public static final Nature ude1;

    /**
     * 地
     */
    public static final Nature ude2;

    /**
     * 得
     */
    public static final Nature ude3;

    /**
     * 所
     */
    public static final Nature usuo;

    /**
     * 等 等等 云云
     */
    public static final Nature udeng;

    /**
     * 一样 一般 似的 般
     */
    public static final Nature uyy;

    /**
     * 的话
     */
    public static final Nature udh;

    /**
     * 来讲 来说 而言 说来
     */
    public static final Nature uls;

    /**
     * 之
     */
    public static final Nature uzhi;
    /**
     * 连 （“连小学生都会”）
     */
    public static final Nature ulian;

    /**
     * 叹词
     */
    public static final Nature e;

    /**
     * 语气词(delete yg)
     */
    public static final Nature y;

    /**
     * 拟声词
     */
    public static final Nature o;

    /**
     * 前缀
     */
    public static final Nature h;

    /**
     * 后缀
     */
    public static final Nature k;

    /**
     * 字符串
     */
    public static final Nature x;

    /**
     * 非语素字
     */
    public static final Nature xx;

    /**
     * 网址URL
     */
    public static final Nature xu;

    /**
     * 标点符号
     */
    public static final Nature w;

    /**
     * 左括号，全角：（ 〔 ［ ｛ 《 【 〖 〈 半角：( [ { <
     */
    public static final Nature wkz;

    /**
     * 右括号，全角：） 〕 ］ ｝ 》 】 〗 〉 半角： ) ] { >
     */
    public static final Nature wky;

    /**
     * 左引号，全角：“ ‘ 『
     */
    public static final Nature wyz;

    /**
     * 右引号，全角：” ’ 』
     */
    public static final Nature wyy;

    /**
     * 句号，全角：。
     */
    public static final Nature wj;

    /**
     * 问号，全角：？ 半角：?
     */
    public static final Nature ww;

    /**
     * 叹号，全角：！ 半角：!
     */
    public static final Nature wt;

    /**
     * 逗号，全角：， 半角：= new Nature("bg",0);
     */
    public static final Nature wd;

    /**
     * 分号，全角：； 半角： = new Nature("bg",0);
     */
    public static final Nature wf;

    /**
     * 顿号，全角：、
     */
    public static final Nature wn;

    /**
     * 冒号，全角：： 半角： :
     */
    public static final Nature wm;

    /**
     * 省略号，全角：…… …
     */
    public static final Nature ws;

    /**
     * 破折号，全角：—— －－ ——－ 半角：--- ----
     */
    public static final Nature wp;

    /**
     * 百分号千分号，全角：％ ‰ 半角：%
     */
    public static final Nature wb;

    /**
     * 单位符号，全角：￥ ＄ ￡ ° ℃ 半角：$
     */
    public static final Nature wh;

    /**
     * 仅用于终##终，不会出现在分词结果中
     */
    public static final Nature end;

    /**
     * 仅用于始##始，不会出现在分词结果中
     */
    public static final Nature begin;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ord;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Nature other = (Nature) obj;
        return ord == other.ord;
    }

    @Override
    public String toString() {
        return name;
    }

    // /**
    // * 词性是否以该前缀开头<br>
    // * 词性根据开头的几个字母可以判断大的类别
    // * @param prefix 前缀
    // * @return 是否以该前缀开头
    // */
    // public boolean startsWith(String prefix)
    // {
    // return toString().startsWith(prefix);
    // }
    //
    // /**
    // * 词性是否以该前缀开头<br>
    // * 词性根据开头的几个字母可以判断大的类别
    // * @param prefix 前缀
    // * @return 是否以该前缀开头
    // */
    // public boolean startsWith(char prefix)
    // {
    // return toString().charAt(0) == prefix;
    // }
    //
    // /**
    // * 词性的首字母<br>
    // * 词性根据开头的几个字母可以判断大的类别
    // * @return
    // */
    // public char firstChar()
    // {
    // return toString().charAt(0);
    // }
    //
    // /**
    // * 安全地将字符串类型的词性转为Enum类型，如果未定义该词性，则返回null
    // * @param name 字符串词性
    // * @return Enum词性
    // */
    // public static Nature fromString(String name)
    // {
    // try
    // {
    // return Nature.valueOf(name);
    // }
    // catch (Exception e)
    // {
    // return null;
    // }
    // }
}