export interface Emotion {
  url: string
  name: string
}

export const useEmotions = () => {
  const replaceEmotions = (content: string) => {
    const emotions = useState<Emotion[]>('emotions').value
    emotions.forEach(emotion => {
      const regex = new RegExp(emotion.name.replace(/[[\]]/g, '\\$&'), 'g')
      content = content.replace(
        regex, 
        `<img src="${emotion.url}" alt="${emotion.name}" class="emotion-img" style="display: inline-block; height: 22px; width: 22px; vertical-align: bottom;">`
      )
    })
    return content
  }

  return {
    emotions: useState<Emotion[]>('emotions', () => {
      const emotions: Emotion[] = []
      const emotionNames = [
        '[呵呵]',
        '[哈哈]',
        '[吐舌]',
        '[啊]',
        '[酷]',
        '[怒]',
        '[开心]',
        '[汗]',
        '[泪]',
        '[黑线]',
        '[鄙视]',
        '[不高兴]',
        '[真棒]',
        '[钱]',
        '[疑问]',
        '[阴脸]',
        '[吐]',
        '[咦]',
        '[委屈]',
        '[花心]',
        '[呼~]',
        '[笑脸]',
        '[冷]',
        '[太开心]',
        '[滑稽]',
        '[勉强]',
        '[狂汗]',
        '[乖]',
        '[睡觉]',
        '[惊哭]',
        '[生气]',
        '[惊讶]',
        '[喷]',
        '[爱心]',
        '[心碎]',
        '[玫瑰]',
        '[礼物]',
        '[彩虹]',
        '[星星月亮]',
        '[太阳]',
        '[钱币]',
        '[灯泡]',
        '[茶杯]',
        '[蛋糕]',
        '[音乐]',
        '[haha]',
        '[胜利]',
        '[大拇指]',
        '[弱]',
        '[OK]',
        '[吃瓜]',
        '[去屎]',
        '[惊悚]',
        '[抛媚眼]',
        '[害羞]',
        '[偷乐]',
        '[阴笑]',
        '[愤怒]',
        '[萎靡]',
        '[哈切]',
        '[笑哭]',
        '[扣鼻屎]',
        '[怒视]',
        '[额~]',
        '[被揍]',
        '[沙发]',
        '[卷纸]',
        '[香蕉]',
        '[屎]',
        '[药丸]',
        '[红领巾]',
        '[蜡烛]',
        '[三条杠]'
      ]
      for (let i = 1; i <= 50; i++) {
        emotions.push({
          url: `/emotion/tieba/${i}.jpg`,
          name: emotionNames[i - 1]
        })
      }
      return emotions
    }),
    replaceEmotions
  }
}
