package com.musicwise.service;

import com.musicwise.model.BlessingPreset;
import com.musicwise.model.PolishTemplate;
import com.musicwise.model.RecipientType;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PolishTemplateService {
    private final List<PolishTemplate> templates = List.of(
            new PolishTemplate(
                    "T001",
                    "给家人的生日",
                    "又长大一岁了，但在我心里，你一直是那个让我觉得安心的人。愿接下来的日子，身体安稳，心也安稳，慢慢过，每一天都不被辜负。",
                    RecipientType.FAMILY,
                    BlessingPreset.BIRTHDAY,
                    "通用"
            ),
            new PolishTemplate(
                    "T002",
                    "给家人的节日",
                    "又到节日了，想和你说一声，不管时间怎么走、日子多忙，能想到你、记挂你，本身就是一件很幸福的事。",
                    RecipientType.FAMILY,
                    BlessingPreset.FESTIVAL,
                    "通用"
            ),
            new PolishTemplate(
                    "T003",
                    "给家人的感谢",
                    "很多话平时不太说出口，但我一直记得你为我做过的那些事。谢谢你一直在，也一直为我撑着。",
                    RecipientType.FAMILY,
                    BlessingPreset.THANKS,
                    "通用"
            ),
            new PolishTemplate(
                    "T004",
                    "给家人的祝贺",
                    "听到这个好消息的时候，我一点也不意外。你一步一步走到现在，本来就值得这些掌声和肯定，真心为你感到骄傲。",
                    RecipientType.FAMILY,
                    BlessingPreset.CONGRATS,
                    "通用"
            ),
            new PolishTemplate(
                    "T005",
                    "给家人的问候",
                    "想对你说一句真心话。家里的温暖一直是我最大的底气。愿你一切安好，忙碌也有力量。",
                    RecipientType.FAMILY,
                    BlessingPreset.NONE,
                    "通用"
            ),
            new PolishTemplate(
                    "T006",
                    "给同学的生日",
                    "生日快乐。希望新的一岁里，你不只是在完成生活，也能慢慢遇见一些让自己真正开心的瞬间。",
                    RecipientType.CLASSMATE,
                    BlessingPreset.BIRTHDAY,
                    "通用"
            ),
            new PolishTemplate(
                    "T007",
                    "给同学的节日",
                    "节日到了，想和你打个招呼。希望最近的生活，对你来说不算太难，也还能留下一点值得期待的东西。",
                    RecipientType.CLASSMATE,
                    BlessingPreset.FESTIVAL,
                    "通用"
            ),
            new PolishTemplate(
                    "T008",
                    "给同学的感谢",
                    "谢谢你在那段时间里的陪伴和帮助。很多事情能走过来，其实都离不开你当时的那份支持。",
                    RecipientType.CLASSMATE,
                    BlessingPreset.THANKS,
                    "通用"
            ),
            new PolishTemplate(
                    "T009",
                    "给同学的祝贺",
                    "看到你走到现在这个位置，真的很替你开心。那些努力和坚持，没有白费，未来也一定会继续往前。",
                    RecipientType.CLASSMATE,
                    BlessingPreset.CONGRATS,
                    "通用"
            ),
            new PolishTemplate(
                    "T010",
                    "给同学的问候",
                    "想对你说一句真心话。一起努力的日子我很珍惜。愿你一切安好，忙碌也有力量。",
                    RecipientType.CLASSMATE,
                    BlessingPreset.NONE,
                    "通用"
            ),
            new PolishTemplate(
                    "T011",
                    "给朋友的生日",
                    "又是一年生日了，希望你依然能做自己，偶尔迷茫、偶尔开心，但始终不丢掉对生活的热情。",
                    RecipientType.FRIEND,
                    BlessingPreset.BIRTHDAY,
                    "通用"
            ),
            new PolishTemplate(
                    "T012",
                    "给朋友的节日",
                    "节日快乐。希望这个节日能让你稍微慢下来，吃点好的，睡个好觉，把自己照顾好。",
                    RecipientType.FRIEND,
                    BlessingPreset.FESTIVAL,
                    "通用"
            ),
            new PolishTemplate(
                    "T013",
                    "给朋友的感谢",
                    "想认真和你说一句谢谢。那些你不经意的陪伴和理解，其实对我来说，一直都很重要。",
                    RecipientType.FRIEND,
                    BlessingPreset.THANKS,
                    "通用"
            ),
            new PolishTemplate(
                    "T014",
                    "给朋友的祝贺",
                    "真心为你感到高兴。你走到今天，不只是运气好，更是因为你真的付出了很多。",
                    RecipientType.FRIEND,
                    BlessingPreset.CONGRATS,
                    "通用"
            ),
            new PolishTemplate(
                    "T015",
                    "给朋友的问候",
                    "想对你说一句真心话。很多时候因为有你才踏实。愿你一切安好，忙碌也有力量。",
                    RecipientType.FRIEND,
                    BlessingPreset.NONE,
                    "通用"
            ),
            new PolishTemplate(
                    "T016",
                    "给爱人的生日",
                    "在你生日这一天，想把所有温柔的祝福都留给你。愿你被生活善待，也愿我能一直在你身边。",
                    RecipientType.LOVER,
                    BlessingPreset.BIRTHDAY,
                    "通用"
            ),
            new PolishTemplate(
                    "T017",
                    "给爱人的节日",
                    "节日到了，比起热闹，我更想和你安静地待一会儿。只要有你在，时间本身就已经很好了。",
                    RecipientType.LOVER,
                    BlessingPreset.FESTIVAL,
                    "通用"
            ),
            new PolishTemplate(
                    "T018",
                    "给爱人的感谢",
                    "谢谢你一直以来的理解和包容。能遇见你、被你陪着走这段路，是我很珍惜的一件事。",
                    RecipientType.LOVER,
                    BlessingPreset.THANKS,
                    "通用"
            ),
            new PolishTemplate(
                    "T019",
                    "给爱人的祝贺",
                    "看到你做到这一步，我真的很替你开心。你所有的努力，我都看在眼里，也一直为你骄傲。",
                    RecipientType.LOVER,
                    BlessingPreset.CONGRATS,
                    "通用"
            ),
            new PolishTemplate(
                    "T020",
                    "给爱人的问候",
                    "想对你说一句真心话。很庆幸你一直在我身边。愿你一切安好，忙碌也有力量。",
                    RecipientType.LOVER,
                    BlessingPreset.NONE,
                    "通用"
            ),
            new PolishTemplate(
                    "T021",
                    "给其他的生日",
                    "生日快乐。感谢你给过的帮助和照顾。愿新的一岁心想事成，平安喜乐。",
                    RecipientType.OTHER,
                    BlessingPreset.BIRTHDAY,
                    "通用"
            ),
            new PolishTemplate(
                    "T022",
                    "给其他的节日",
                    "节日快乐。感谢你给过的帮助和照顾。愿你在节日里放松下来，心里踏实。",
                    RecipientType.OTHER,
                    BlessingPreset.FESTIVAL,
                    "通用"
            ),
            new PolishTemplate(
                    "T023",
                    "给其他的感谢",
                    "谢谢你。你的照顾我一直记在心里。愿你被温柔对待，日子顺心。",
                    RecipientType.OTHER,
                    BlessingPreset.THANKS,
                    "通用"
            ),
            new PolishTemplate(
                    "T024",
                    "给其他的祝贺",
                    "恭喜你取得新的进展。感谢你给过的帮助和照顾。愿你继续保持热爱，前路顺利。",
                    RecipientType.OTHER,
                    BlessingPreset.CONGRATS,
                    "通用"
            ),
            new PolishTemplate(
                    "T025",
                    "给其他的问候",
                    "想对你说一句真心话。感谢你给过的帮助和照顾。愿你一切安好，忙碌也有力量。",
                    RecipientType.OTHER,
                    BlessingPreset.NONE,
                    "通用"
            ),
            new PolishTemplate(
                    "T026",
                    "生日祝福",
                    "生日快乐。这份心意想送给你。愿新的一岁心想事成，平安喜乐。",
                    RecipientType.NONE,
                    BlessingPreset.BIRTHDAY,
                    "通用"
            ),
            new PolishTemplate(
                    "T027",
                    "节日祝福",
                    "节日快乐。这份心意想送给你。愿你在节日里放松下来，心里踏实。",
                    RecipientType.NONE,
                    BlessingPreset.FESTIVAL,
                    "通用"
            ),
            new PolishTemplate(
                    "T028",
                    "感谢祝福",
                    "谢谢你。这份心意想送给你。愿你被温柔对待，日子顺心。",
                    RecipientType.NONE,
                    BlessingPreset.THANKS,
                    "通用"
            ),
            new PolishTemplate(
                    "T029",
                    "祝贺祝福",
                    "恭喜你取得新的进展。这份心意想送给你。愿你继续保持热爱，前路顺利。",
                    RecipientType.NONE,
                    BlessingPreset.CONGRATS,
                    "通用"
            ),
            new PolishTemplate(
                    "T030",
                    "一句问候",
                    "想对你说一句真心话。这份心意想送给你。愿你一切安好，忙碌也有力量。",
                    RecipientType.NONE,
                    BlessingPreset.NONE,
                    "通用"
            )
    );

    public List<PolishTemplate> getTemplates() {
        return templates;
    }
}
