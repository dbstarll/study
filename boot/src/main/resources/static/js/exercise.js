function getExerciseLevel(exerciseWord, exerciseKey) {
    if (undefined !== exerciseWord.masterPercents) {
        var percent = exerciseWord.masterPercents[exerciseKey];
        if (undefined !== percent) {
            return Math.floor(percent.percent);
        }
    }
    return 0;
}

function noMoreExercise(tips, spell) {
    tips.empty();
    spell.empty().append($.html.div({}, '近期内没有更多的练习了，休息一会再做？')).append($.html.div({}, "或者你还可以：").append($.html.a({
        href: contextUrl('/exercise-book')
    }, '添加新单词').addClass('btn btn-primary')));
}

function replaceStars(level, stars, today, all) {
    stars.empty();
    stars.append($.html.span().addClass('small').text(all.correct + '/' + all.total + ' __'));

    var starCount = level + 1;
    while (starCount >= 2) {
        starCount -= 2;
        stars.append(icon('glyphicon-star'));
    }
    if (starCount > 0) {
        stars.append(icon('glyphicon-star-empty'));
    }

    stars.append($.html.span().addClass('small').text('__ ' + today.correct + '/' + today.total));
}
