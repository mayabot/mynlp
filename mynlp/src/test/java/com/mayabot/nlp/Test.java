package com.mayabot.nlp;

import com.mayabot.nlp.segment.Lexers;

public class Test {
    public static void main(String[] args) {
        System.out.println(Lexers.core().scan(
                "瑞金二路街道举办“法官进社区”系列讲座活动信息来源：瑞金二路街道发布时间：2014-03-31字体： 【】日前，瑞金二路街道举办“法官进社区”系列讲座活动，邀请区法院民一庭审判长来讲《婚姻法》、《继承法》等法律。各居委调委会主任、委员共计70余人参加。法官对《婚姻法》、《继承法》等法律进行了多角度、多层次、深入浅出的精彩阐述，并结合自己的工作经历，用实际案例对相关法律条款进行了耐心、详细的讲解。通过开展“法官进社区”活动，统一建立社区法官制度和社区司法联络员制度，积极参与社会矛盾化解和社会管理创新，促进社区居民自治、民主管理。"));
    }
}