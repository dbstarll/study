var ExchangeKey = {
    pl: '复数',
    third: '第三人称单数',
    past: '过去式',
    done: '过去分词',
    ing: '现在分词',
    er: '比较级',
    est: '最高级'
};

var PhoneticKey = {
    en: '英',
    am: '美'
};

var PartKeyReplace = {
    '_int': 'int.',
    'link_v': 'link-v.'
};

function parsePartKey(keys) {
    var arr = new Array();
    $.each(keys, function (idx, key) {
        var partKey = PartKeyReplace[key];
        arr.push(partKey ? partKey : key + '.');
    });
    return arr.join('& ');
}

function contextUrl(url) {
    var contextPath = $('#contextPath').data('context-path');
    if ('string' === typeof contextPath && url.startsWith('/')) {
        return contextPath + url.substring(1);
    } else {
        return url;
    }
}

function showWord(word, hidden, dictionary) {
    if (undefined === dictionary) {
        dictionary = $('#dictionary');
    }
    if (dictionary.length > 0) {
        replaceTitle(word, $('.dictionary-title', dictionary));
        replacePhonetics(word, $('.dictionary-spell', dictionary));
        replaceParts(word, $('.dictionary-comment', dictionary));
        replaceExchanges(word, $('.dictionary-exchange', dictionary));
        if (hidden) {
            return dictionary;
        } else {
            return dictionary.removeClass('hidden');
        }
    }
}

function icon(glyphicon) {
    return $.html.span().addClass('glyphicon').addClass(glyphicon);
}

function div(classes) {
    var item = $.html.div();
    if ('string' === typeof classes) {
        item.addClass(classes);
    }
    return item;
}

function replaceTitle(word, container) {
    if (container.length > 0) {
        var item = $.html.set('<h3/>').addClass('strong').text(word.name);
        if (false === word.cri) {
            item.append(icon('glyphicon-tag').addClass('small'));
        }
        if (undefined === word.id) {
            item.append(icon('glyphicon-star-empty').addClass('small'));
        }
        container.empty().append(item);
    }
}

function audioPauseAll(container) {
    var audio = undefined === container ? $('audio') : $('audio', container);
    $.each(audio, function (_, audio) {
        if (audio.loop) {
            audio.pause();
        }
    });
}

function replacePhonetics(word, container) {
    if (container.length > 0) {
        container.empty();
        $.each(word.phonetics, function (_, phonetic) {
            var item = $.html.span().addClass('phonetic-transcription').addClass(phonetic.key);
            item.append($.html.span({}, PhoneticKey[phonetic.key]));
            item.append($.html.set('<b/>', {}, '[' + phonetic.symbol + ']').addClass('phonetic-symbol')).append(' ');
            if (phonetic.voiceId) {
                var audio = $.html.set('<audio/>', {
                    preload: 'none',
                    src: contextUrl('/voice/' + phonetic.voiceId)
                }).get(0);
                item.append(audio).append(
                    $.html.a().addClass('op-sound').append($.html.span().addClass('icon-sound sound-btn').click(function () {
                        if (audio.loop) {
                            audio.loop = false;
                        }
                        audio.play();
                    })));
                item.append($.html.a().addClass('op-repeat data-hover-tip').append(
                    $.html.span().addClass('icon-repeat sound-btn').click(function () {
                        if (audio.loop) {
                            audio.loop = false;
                            audio.pause();
                        } else {
                            audio.loop = true;
                            audio.play();
                        }
                    })));
            }
            if ('en' === phonetic.key) {
                container.prepend(item);
            } else {
                container.append(item);
            }
        });
    }
}

function replaceParts(word, container) {
    if (container.length > 0) {
        container.empty();
        $.each(word.parts, function (_, part) {
            var item = $.html.p();
            item.append($.html.set('<b/>', {}, parsePartKey(part.key)));
            var means = $.html.set('<strong/>').addClass('dict-comment-mean');
            $.each(part.means, function (idxMean, mean) {
                if (idxMean !== 0) {
                    means.append($.html.span({}, ';').addClass('dict-margin'));
                }
                means.append($.html.span({}, mean));
            });
            item.append(means);
            container.append(item);
        });
    }
}

function exchangeLink(word) {
    return $.html.a({}, word).addClass('sec-trans').click(function () {
        $.getJSON(contextUrl('/word/' + word), function (data) {
            showWord(data);
        });
    });
}

function replaceExchanges(word, container) {
    if (container.length > 0) {
        container.empty();
        $.each(word.exchanges, function (_, exchange) {
            var item = $.html.p().append(
                $.html.span({}, ExchangeKey[exchange.key] + '：').addClass('word-can-trans').append(
                    exchangeLink(exchange.word)));
            container.append(item);
        });
        if (undefined !== word.wordId) {
            $.getJSON(contextUrl('/word/' + word.wordId + '/id'), function (data) {
                var item = $.html.p()
                    .append($.html.span({}, ' 原型：').addClass('word-can-trans').append(exchangeLink(data.name)));
                container.append(item);
            });
        }
    }
}

function exerciseAnswer(data, dictionary, exercise, tips, spell, exerciseKey, level, spellWord, startExercise) {
    var correct = data.word.name === spellWord;
    $('.hidden', dictionary).removeClass('hidden');
    $('audio.' + (correct ? 'right' : 'wrong')).get(0).play();

    $.ajax(contextUrl('/exercise-word/' + data.exerciseWord.id + '/' + exerciseKey + '/' + level + '/' + spellWord), {
        dataType: "json",
        method: 'PUT'
    }).done(function (next) {
        tips.empty().append($.html.button().addClass('btn btn-primary').text('下一题').click(function () {
            startExercise(next, dictionary, exercise, tips, spell);
        }));
    }).fail(function (jqxhr, textStatus, error) {
        if (404 === jqxhr.status) {
            noMoreExercise(tips, spell);
        }
    });
}
