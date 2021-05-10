package com.mayabot.nlp.module.pinyin.model

import com.mayabot.nlp.character.ChineseCharInfos
import com.mayabot.nlp.common.utils.CartesianList

/**
 * 没有语调的拼音
 */
enum class SimplePinyin(
    val shengmu: Shengmu,
    val yunmu: Yunmu
) {
    a(Shengmu.none, Yunmu.a),
    ai(Shengmu.none, Yunmu.ai),
    an(Shengmu.none, Yunmu.an),
    ang(Shengmu.none, Yunmu.ang),
    ao(Shengmu.none, Yunmu.ao),
    ba(Shengmu.b, Yunmu.a),
    bai(Shengmu.b, Yunmu.ai),
    ban(Shengmu.b, Yunmu.an),
    bang(Shengmu.b, Yunmu.ang),
    bao(Shengmu.b, Yunmu.ao),
    bei(Shengmu.b, Yunmu.ei),
    ben(Shengmu.b, Yunmu.en),
    beng(Shengmu.b, Yunmu.eng),
    bi(Shengmu.b, Yunmu.i),
    bian(Shengmu.b, Yunmu.ian),
    biao(Shengmu.b, Yunmu.iao),
    bie(Shengmu.b, Yunmu.ie),
    bin(Shengmu.b, Yunmu.`in`),
    bing(Shengmu.b, Yunmu.ing),
    bo(Shengmu.b, Yunmu.o),
    bu(Shengmu.b, Yunmu.u),
    ca(Shengmu.c, Yunmu.a),
    cai(Shengmu.c, Yunmu.ai),
    can(Shengmu.c, Yunmu.an),
    cang(Shengmu.c, Yunmu.ang),
    cao(Shengmu.c, Yunmu.ao),
    ce(Shengmu.c, Yunmu.e),
    cen(Shengmu.c, Yunmu.en),
    ceng(Shengmu.c, Yunmu.eng),
    cha(Shengmu.ch, Yunmu.a),
    chai(Shengmu.ch, Yunmu.ai),
    chan(Shengmu.ch, Yunmu.an),
    chang(Shengmu.ch, Yunmu.ang),
    chao(Shengmu.ch, Yunmu.ao),
    che(Shengmu.ch, Yunmu.e),
    chen(Shengmu.ch, Yunmu.en),
    cheng(Shengmu.ch, Yunmu.eng),
    chi(Shengmu.ch, Yunmu.i),
    chong(Shengmu.ch, Yunmu.ong),
    chou(Shengmu.ch, Yunmu.ou),
    chu(Shengmu.ch, Yunmu.u),
    chua(Shengmu.ch, Yunmu.ua),
    chuai(Shengmu.ch, Yunmu.uai),
    chuan(Shengmu.ch, Yunmu.uan),
    chuang(Shengmu.ch, Yunmu.uang),
    chui(Shengmu.ch, Yunmu.ui),
    chun(Shengmu.ch, Yunmu.un),
    chuo(Shengmu.ch, Yunmu.uo),
    ci(Shengmu.c, Yunmu.i),
    cong(Shengmu.c, Yunmu.ong),
    cou(Shengmu.c, Yunmu.ou),
    cu(Shengmu.c, Yunmu.u),
    cuan(Shengmu.c, Yunmu.uan),
    cui(Shengmu.c, Yunmu.ui),
    cun(Shengmu.c, Yunmu.un),
    cuo(Shengmu.c, Yunmu.uo),
    da(Shengmu.d, Yunmu.a),
    dai(Shengmu.d, Yunmu.ai),
    dan(Shengmu.d, Yunmu.an),
    dang(Shengmu.d, Yunmu.ang),
    dao(Shengmu.d, Yunmu.ao),
    de(Shengmu.d, Yunmu.e),
    dei(Shengmu.d, Yunmu.ei),
    den(Shengmu.d, Yunmu.en),
    deng(Shengmu.d, Yunmu.eng),
    di(Shengmu.d, Yunmu.i),
    dia(Shengmu.d, Yunmu.ia),
    dian(Shengmu.d, Yunmu.ian),
    diao(Shengmu.d, Yunmu.iao),
    die(Shengmu.d, Yunmu.ie),
    ding(Shengmu.d, Yunmu.ing),
    diu(Shengmu.d, Yunmu.iu),
    dong(Shengmu.d, Yunmu.ong),
    dou(Shengmu.d, Yunmu.ou),
    du(Shengmu.d, Yunmu.u),
    duan(Shengmu.d, Yunmu.uan),
    dui(Shengmu.d, Yunmu.ui),
    dun(Shengmu.d, Yunmu.un),
    duo(Shengmu.d, Yunmu.uo),
    e(Shengmu.none, Yunmu.e),
    ei(Shengmu.none, Yunmu.ei),
    en(Shengmu.none, Yunmu.en),
    eng(Shengmu.none, Yunmu.eng),
    er(Shengmu.none, Yunmu.er),
    fa(Shengmu.f, Yunmu.a),
    fan(Shengmu.f, Yunmu.an),
    fang(Shengmu.f, Yunmu.ang),
    fei(Shengmu.f, Yunmu.ei),
    fen(Shengmu.f, Yunmu.en),
    feng(Shengmu.f, Yunmu.eng),
    fiao(Shengmu.f, Yunmu.iao),
    fo(Shengmu.f, Yunmu.o),
    fou(Shengmu.f, Yunmu.ou),
    fu(Shengmu.f, Yunmu.u),
    ga(Shengmu.g, Yunmu.a),
    gai(Shengmu.g, Yunmu.ai),
    gan(Shengmu.g, Yunmu.an),
    gang(Shengmu.g, Yunmu.ang),
    gao(Shengmu.g, Yunmu.ao),
    ge(Shengmu.g, Yunmu.e),
    gei(Shengmu.g, Yunmu.ei),
    gen(Shengmu.g, Yunmu.en),
    geng(Shengmu.g, Yunmu.eng),
    gong(Shengmu.g, Yunmu.ong),
    gou(Shengmu.g, Yunmu.ou),
    gu(Shengmu.g, Yunmu.u),
    gua(Shengmu.g, Yunmu.ua),
    guai(Shengmu.g, Yunmu.uai),
    guan(Shengmu.g, Yunmu.uan),
    guang(Shengmu.g, Yunmu.uang),
    gui(Shengmu.g, Yunmu.ui),
    gun(Shengmu.g, Yunmu.un),
    guo(Shengmu.g, Yunmu.uo),
    ha(Shengmu.h, Yunmu.a),
    hai(Shengmu.h, Yunmu.ai),
    han(Shengmu.h, Yunmu.an),
    hang(Shengmu.h, Yunmu.ang),
    hao(Shengmu.h, Yunmu.ao),
    he(Shengmu.h, Yunmu.e),
    hei(Shengmu.h, Yunmu.ei),
    hen(Shengmu.h, Yunmu.en),
    heng(Shengmu.h, Yunmu.eng),
    hong(Shengmu.h, Yunmu.ong),
    hou(Shengmu.h, Yunmu.ou),
    hu(Shengmu.h, Yunmu.u),
    hua(Shengmu.h, Yunmu.ua),
    huai(Shengmu.h, Yunmu.uai),
    huan(Shengmu.h, Yunmu.uan),
    huang(Shengmu.h, Yunmu.uang),
    hui(Shengmu.h, Yunmu.ui),
    hun(Shengmu.h, Yunmu.un),
    huo(Shengmu.h, Yunmu.uo),
    ja(Shengmu.j, Yunmu.a),
    ji(Shengmu.j, Yunmu.i),
    jia(Shengmu.j, Yunmu.ia),
    jian(Shengmu.j, Yunmu.ian),
    jiang(Shengmu.j, Yunmu.iang),
    jiao(Shengmu.j, Yunmu.iao),
    jie(Shengmu.j, Yunmu.ie),
    jin(Shengmu.j, Yunmu.`in`),
    jing(Shengmu.j, Yunmu.ing),
    jiong(Shengmu.j, Yunmu.iong),
    jiu(Shengmu.j, Yunmu.iu),
    ju(Shengmu.j, Yunmu.u),
    juan(Shengmu.j, Yunmu.uan),
    jue(Shengmu.j, Yunmu.ue),
    jun(Shengmu.j, Yunmu.un),
    ka(Shengmu.k, Yunmu.a),
    kai(Shengmu.k, Yunmu.ai),
    kan(Shengmu.k, Yunmu.an),
    kang(Shengmu.k, Yunmu.ang),
    kao(Shengmu.k, Yunmu.ao),
    ke(Shengmu.k, Yunmu.e),
    kei(Shengmu.k, Yunmu.ei),
    ken(Shengmu.k, Yunmu.en),
    keng(Shengmu.k, Yunmu.eng),
    kong(Shengmu.k, Yunmu.ong),
    kou(Shengmu.k, Yunmu.ou),
    ku(Shengmu.k, Yunmu.u),
    kua(Shengmu.k, Yunmu.ua),
    kuai(Shengmu.k, Yunmu.uai),
    kuan(Shengmu.k, Yunmu.uan),
    kuang(Shengmu.k, Yunmu.uang),
    kui(Shengmu.k, Yunmu.ui),
    kun(Shengmu.k, Yunmu.un),
    kuo(Shengmu.k, Yunmu.uo),
    la(Shengmu.l, Yunmu.a),
    lai(Shengmu.l, Yunmu.ai),
    lan(Shengmu.l, Yunmu.an),
    lang(Shengmu.l, Yunmu.ang),
    lao(Shengmu.l, Yunmu.ao),
    le(Shengmu.l, Yunmu.e),
    lei(Shengmu.l, Yunmu.ei),
    leng(Shengmu.l, Yunmu.eng),
    li(Shengmu.l, Yunmu.i),
    lia(Shengmu.l, Yunmu.ia),
    lian(Shengmu.l, Yunmu.ian),
    liang(Shengmu.l, Yunmu.iang),
    liao(Shengmu.l, Yunmu.iao),
    lie(Shengmu.l, Yunmu.ie),
    lin(Shengmu.l, Yunmu.`in`),
    ling(Shengmu.l, Yunmu.ing),
    liu(Shengmu.l, Yunmu.iu),
    lo(Shengmu.l, Yunmu.o),
    long(Shengmu.l, Yunmu.ong),
    lou(Shengmu.l, Yunmu.ou),
    lu(Shengmu.l, Yunmu.u),
    luan(Shengmu.l, Yunmu.uan),
    lun(Shengmu.l, Yunmu.un),
    luo(Shengmu.l, Yunmu.uo),
    lv(Shengmu.l, Yunmu.v),
    lve(Shengmu.l, Yunmu.ve),
    ma(Shengmu.m, Yunmu.a),
    mai(Shengmu.m, Yunmu.ai),
    man(Shengmu.m, Yunmu.an),
    mang(Shengmu.m, Yunmu.ang),
    mao(Shengmu.m, Yunmu.ao),
    me(Shengmu.m, Yunmu.e),
    mei(Shengmu.m, Yunmu.ei),
    men(Shengmu.m, Yunmu.en),
    meng(Shengmu.m, Yunmu.eng),
    mi(Shengmu.m, Yunmu.i),
    mian(Shengmu.m, Yunmu.ian),
    miao(Shengmu.m, Yunmu.iao),
    mie(Shengmu.m, Yunmu.ie),
    min(Shengmu.m, Yunmu.`in`),
    ming(Shengmu.m, Yunmu.ing),
    miu(Shengmu.m, Yunmu.iu),
    mo(Shengmu.m, Yunmu.o),
    mou(Shengmu.m, Yunmu.ou),
    mu(Shengmu.m, Yunmu.u),
    na(Shengmu.n, Yunmu.a),
    nai(Shengmu.n, Yunmu.ai),
    nan(Shengmu.n, Yunmu.an),
    nang(Shengmu.n, Yunmu.ang),
    nao(Shengmu.n, Yunmu.ao),
    ne(Shengmu.n, Yunmu.e),
    nei(Shengmu.n, Yunmu.ei),
    nen(Shengmu.n, Yunmu.en),
    neng(Shengmu.n, Yunmu.eng),
    ni(Shengmu.n, Yunmu.i),
    nian(Shengmu.n, Yunmu.ian),
    niang(Shengmu.n, Yunmu.iang),
    niao(Shengmu.n, Yunmu.iao),
    nie(Shengmu.n, Yunmu.ie),
    nin(Shengmu.n, Yunmu.`in`),
    ning(Shengmu.n, Yunmu.ing),
    niu(Shengmu.n, Yunmu.iu),
    nong(Shengmu.n, Yunmu.ong),
    nou(Shengmu.n, Yunmu.ou),
    nu(Shengmu.n, Yunmu.u),
    nuan(Shengmu.n, Yunmu.uan),
    nun(Shengmu.n, Yunmu.un),
    nuo(Shengmu.n, Yunmu.uo),
    nv(Shengmu.n, Yunmu.v),
    nve(Shengmu.n, Yunmu.ue),
    o(Shengmu.none, Yunmu.o),
    ou(Shengmu.none, Yunmu.ou),
    pa(Shengmu.p, Yunmu.a),
    pai(Shengmu.p, Yunmu.ai),
    pan(Shengmu.p, Yunmu.an),
    pang(Shengmu.p, Yunmu.ang),
    pao(Shengmu.p, Yunmu.ao),
    pei(Shengmu.p, Yunmu.ei),
    pen(Shengmu.p, Yunmu.en),
    peng(Shengmu.p, Yunmu.eng),
    pi(Shengmu.p, Yunmu.i),
    pian(Shengmu.p, Yunmu.ian),
    piao(Shengmu.p, Yunmu.iao),
    pie(Shengmu.p, Yunmu.ie),
    pin(Shengmu.p, Yunmu.`in`),
    ping(Shengmu.p, Yunmu.ing),
    po(Shengmu.p, Yunmu.o),
    pou(Shengmu.p, Yunmu.ou),
    pu(Shengmu.p, Yunmu.u),
    qi(Shengmu.q, Yunmu.i),
    qia(Shengmu.q, Yunmu.ia),
    qian(Shengmu.q, Yunmu.ian),
    qiang(Shengmu.q, Yunmu.iang),
    qiao(Shengmu.q, Yunmu.iao),
    qie(Shengmu.q, Yunmu.ie),
    qin(Shengmu.q, Yunmu.`in`),
    qing(Shengmu.q, Yunmu.ing),
    qiong(Shengmu.q, Yunmu.iong),
    qiu(Shengmu.q, Yunmu.iu),
    qu(Shengmu.q, Yunmu.u),
    quan(Shengmu.q, Yunmu.uan),
    que(Shengmu.q, Yunmu.ue),
    qun(Shengmu.q, Yunmu.un),
    ran(Shengmu.r, Yunmu.an),
    rang(Shengmu.r, Yunmu.ang),
    rao(Shengmu.r, Yunmu.ao),
    re(Shengmu.r, Yunmu.e),
    ren(Shengmu.r, Yunmu.en),
    reng(Shengmu.r, Yunmu.eng),
    ri(Shengmu.r, Yunmu.i),
    rong(Shengmu.r, Yunmu.ong),
    rou(Shengmu.r, Yunmu.ou),
    ru(Shengmu.r, Yunmu.u),
    ruan(Shengmu.r, Yunmu.uan),
    rui(Shengmu.r, Yunmu.ui),
    run(Shengmu.r, Yunmu.un),
    ruo(Shengmu.r, Yunmu.uo),
    sa(Shengmu.s, Yunmu.a),
    sai(Shengmu.s, Yunmu.ai),
    san(Shengmu.s, Yunmu.an),
    sang(Shengmu.s, Yunmu.ang),
    sao(Shengmu.s, Yunmu.ao),
    se(Shengmu.s, Yunmu.e),
    sen(Shengmu.s, Yunmu.en),
    seng(Shengmu.s, Yunmu.eng),
    sha(Shengmu.sh, Yunmu.a),
    shai(Shengmu.sh, Yunmu.ai),
    shan(Shengmu.sh, Yunmu.an),
    shang(Shengmu.sh, Yunmu.ang),
    shao(Shengmu.sh, Yunmu.ao),
    she(Shengmu.sh, Yunmu.e),
    shei(Shengmu.sh, Yunmu.ei),
    shen(Shengmu.sh, Yunmu.en),
    sheng(Shengmu.sh, Yunmu.eng),
    shi(Shengmu.sh, Yunmu.i),
    shou(Shengmu.sh, Yunmu.ou),
    shu(Shengmu.sh, Yunmu.u),
    shua(Shengmu.sh, Yunmu.ua),
    shuai(Shengmu.sh, Yunmu.uai),
    shuan(Shengmu.sh, Yunmu.uan),
    shuang(Shengmu.sh, Yunmu.uang),
    shui(Shengmu.sh, Yunmu.ui),
    shun(Shengmu.sh, Yunmu.un),
    shuo(Shengmu.sh, Yunmu.uo),
    si(Shengmu.s, Yunmu.i),
    song(Shengmu.s, Yunmu.ong),
    sou(Shengmu.s, Yunmu.ou),
    su(Shengmu.s, Yunmu.u),
    suan(Shengmu.s, Yunmu.uan),
    sui(Shengmu.s, Yunmu.ui),
    sun(Shengmu.s, Yunmu.un),
    suo(Shengmu.s, Yunmu.uo),
    ta(Shengmu.t, Yunmu.a),
    tai(Shengmu.t, Yunmu.ai),
    tan(Shengmu.t, Yunmu.an),
    tang(Shengmu.t, Yunmu.ang),
    tao(Shengmu.t, Yunmu.ao),
    te(Shengmu.t, Yunmu.e),
    teng(Shengmu.t, Yunmu.eng),
    ti(Shengmu.t, Yunmu.i),
    tian(Shengmu.t, Yunmu.ian),
    tiao(Shengmu.t, Yunmu.iao),
    tie(Shengmu.t, Yunmu.ie),
    ting(Shengmu.t, Yunmu.ing),
    tong(Shengmu.t, Yunmu.ong),
    tou(Shengmu.t, Yunmu.ou),
    tu(Shengmu.t, Yunmu.u),
    tuan(Shengmu.t, Yunmu.uan),
    tui(Shengmu.t, Yunmu.ui),
    tun(Shengmu.t, Yunmu.un),
    tuo(Shengmu.t, Yunmu.uo),
    wa(Shengmu.w, Yunmu.a),
    wai(Shengmu.w, Yunmu.ai),
    wan(Shengmu.w, Yunmu.an),
    wang(Shengmu.w, Yunmu.ang),
    wei(Shengmu.w, Yunmu.ei),
    wen(Shengmu.w, Yunmu.en),
    weng(Shengmu.w, Yunmu.eng),
    wo(Shengmu.w, Yunmu.o),
    wu(Shengmu.w, Yunmu.u),
    xi(Shengmu.x, Yunmu.i),
    xia(Shengmu.x, Yunmu.ia),
    xian(Shengmu.x, Yunmu.ian),
    xiang(Shengmu.x, Yunmu.iang),
    xiao(Shengmu.x, Yunmu.iao),
    xie(Shengmu.x, Yunmu.ie),
    xin(Shengmu.x, Yunmu.`in`),
    xing(Shengmu.x, Yunmu.ing),
    xiong(Shengmu.x, Yunmu.iong),
    xiu(Shengmu.x, Yunmu.iu),
    xu(Shengmu.x, Yunmu.u),
    xuan(Shengmu.x, Yunmu.uan),
    xue(Shengmu.x, Yunmu.ue),
    xun(Shengmu.x, Yunmu.un),
    ya(Shengmu.y, Yunmu.a),
    yai(Shengmu.y, Yunmu.ai),
    yan(Shengmu.y, Yunmu.an),
    yang(Shengmu.y, Yunmu.ang),
    yao(Shengmu.y, Yunmu.ao),
    ye(Shengmu.y, Yunmu.e),
    yi(Shengmu.y, Yunmu.i),
    yin(Shengmu.y, Yunmu.`in`),
    ying(Shengmu.y, Yunmu.ing),
    yo(Shengmu.y, Yunmu.o),
    yong(Shengmu.y, Yunmu.ong),
    you(Shengmu.y, Yunmu.ou),
    yu(Shengmu.y, Yunmu.u),
    yuan(Shengmu.y, Yunmu.uan),
    yue(Shengmu.y, Yunmu.ue),
    yun(Shengmu.y, Yunmu.un),
    za(Shengmu.z, Yunmu.a),
    zai(Shengmu.z, Yunmu.ai),
    zan(Shengmu.z, Yunmu.an),
    zang(Shengmu.z, Yunmu.ang),
    zao(Shengmu.z, Yunmu.ao),
    ze(Shengmu.z, Yunmu.e),
    zei(Shengmu.z, Yunmu.ei),
    zen(Shengmu.z, Yunmu.en),
    zeng(Shengmu.z, Yunmu.eng),
    zha(Shengmu.zh, Yunmu.a),
    zhai(Shengmu.zh, Yunmu.ai),
    zhan(Shengmu.zh, Yunmu.an),
    zhang(Shengmu.zh, Yunmu.ang),
    zhao(Shengmu.zh, Yunmu.ao),
    zhe(Shengmu.zh, Yunmu.e),
    zhei(Shengmu.zh, Yunmu.ei),
    zhen(Shengmu.zh, Yunmu.en),
    zheng(Shengmu.zh, Yunmu.eng),
    zhi(Shengmu.zh, Yunmu.i),
    zhong(Shengmu.zh, Yunmu.ong),
    zhou(Shengmu.zh, Yunmu.ou),
    zhu(Shengmu.zh, Yunmu.u),
    zhua(Shengmu.zh, Yunmu.ua),
    zhuai(Shengmu.zh, Yunmu.uai),
    zhuan(Shengmu.zh, Yunmu.uan),
    zhuang(Shengmu.zh, Yunmu.uang),
    zhui(Shengmu.zh, Yunmu.ui),
    zhun(Shengmu.zh, Yunmu.un),
    zhuo(Shengmu.zh, Yunmu.uo),
    zi(Shengmu.z, Yunmu.i),
    zong(Shengmu.z, Yunmu.ong),
    zou(Shengmu.z, Yunmu.ou),
    zu(Shengmu.z, Yunmu.u),
    zuan(Shengmu.z, Yunmu.uan),
    zui(Shengmu.z, Yunmu.ui),
    zun(Shengmu.z, Yunmu.un),
    zuo(Shengmu.z, Yunmu.uo),
    none(Shengmu.none, Yunmu.none);

    val firstChar = if (this.shengmu != Shengmu.none && this.yunmu != Yunmu.none) this.name.first() else ' '

    companion object {

        /**
         * 各种拼写可能性。（多音字）
         */
        @JvmStatic
        fun textAllPinyin(text: String): List<List<SimplePinyin>> {
            val map = HashMap<Char, List<SimplePinyin>>()
            text.forEach { ch ->
                if (map.containsKey(ch)) {
                    return@forEach
                }
                map[ch] = ChineseCharInfos[ch]?.pinyin ?: listOf(SimplePinyin.none)
            }
            val list = ArrayList<ArrayList<Pair<Char, SimplePinyin>>>()

            for (entry in map) {
                val row = ArrayList<Pair<Char, SimplePinyin>>()
                for (pinyin in entry.value) {
                    row += entry.key to pinyin
                }
                list += row
            }

            val result = ArrayList<ArrayList<SimplePinyin>>()
            CartesianList.create(list).forEach {
                val x: Map<Char, SimplePinyin> = it.toMap()
                val row = ArrayList<SimplePinyin>(text.length)
                text.forEach { ch ->
                    row += x[ch]!!
                }
                result += row
            }
            return result
        }
    }
}

/**
 * 返回各种拼写可能性
 */
fun String.textAllPinyin() = SimplePinyin.textAllPinyin(this)
