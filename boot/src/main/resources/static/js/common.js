$.extend({
  logger: function () {
    function init() {
      var defaultLogger = console.log;
      return {
        log: defaultLogger,
        onlog: function (handler) {
          this.log = 'function' === typeof handler ? handler : defaultLogger;
        },
        li: function (board, max, ts) {
          this.onlog(function (msg, icon) {
            if (board.children().length > max) {
              board.children(':first').remove();
            }
            var line = $.html.li().addClass('small');
            if (ts) {
              line.text(new Date().format().concat(' - '));
            }
            if ('string' === typeof icon) {
              line.append($.html.span({
                'aria-hidden': true
              }).addClass('glyphicon').addClass(icon)).append(' ');
            }
            line.append(msg);
            board.append(line);
          });
        }
      };
    }

    return init();
  }(),

  html: function () {
    function get(element) {
      if (undefined === element) {
        return $('<div>');
      } else if ('string' === typeof element) {
        return $(element);
      } else if ('object' === typeof element) {
        if (element.constructor == $) {// jQuery Object
          return element;
        } else if ('HTML' === element.constructor.name.slice(0, 4)) {
          return $(element);
        }
      }
    }

    function set(element, attrs, text) {
      var obj = get(element);
      if (undefined !== obj) {
        if ('object' === typeof attrs) {
          $.each(attrs, function (k, v) {
            if ('string' === typeof k && undefined !== v) {
              obj.attr(k, v);
            }
          });
        }
        if (undefined !== text) {
          obj.text(text);
        }
      }
      return obj;
    }

    function merge(options, defaultOptions) {
      if ('object' === typeof options) {
        $.each(defaultOptions, function (k, v) {
          if (undefined === options[k]) {
            options[k] = v;
          }
        });
        return options;
      } else {
        return defaultOptions;
      }
    }

    function unescape(text) {
      return set('<span>').html(text).text();
    }

    function escape(text) {
      return set('<span>', {}, text).html().replace(/\"/g, '&quot;').replace(/\'/g, '&apos;').replace(/\(/g, '&#40;')
          .replace(/\)/g, '&#41;');
    }

    function escapeAll(data) {
      if ('object' === typeof data) {
        if ($.isArray(data)) {
          $.each(data, function (idx, value) {
            if ('string' === typeof value) {
              data[idx] = escape(value);
            } else {
              escapeAll(value);
            }
          });
        } else {
          $.each(data, function (key, value) {
            var escapeKey = escape(key);
            if (escapeKey !== key) {
              data[escapeKey] = data[key];
              delete data[key];
              key = escapeKey;
            }
            if ('string' === typeof value) {
              data[key] = escape(value);
            } else {
              escapeAll(value);
            }
          });
        }
      }
      return data;
    }

    function init() {
      return {
        get: get,
        set: set,
        merge: merge,
        unescape: unescape,
        escape: escape,
        escapeAll: escapeAll,
        p: function (attrs, text) {
          return set('<p>', attrs, text);
        },
        div: function (attrs, text) {
          return set('<div>', attrs, text);
        },
        span: function (attrs, text) {
          return set('<span>', attrs, text);
        },
        badge: function (attrs, text) {
          return this.span(attrs, text).addClass('badge');
        },
        img: function (attrs, text) {
          return set('<img>', attrs, text);
        },
        a: function (attrs, text) {
          return set('<a>', attrs, text);
        },
        button: function (attrs, text) {
          return set('<button>', attrs, text);
        },
        embed: function (attrs, text) {
          return set('<embed>', attrs, text);
        },
        li: function (attrs, text) {
          return set('<li>', attrs, text);
        },
        table: function (attrs, text) {
          return set('<table>', attrs, text);
        },
        tr: function (attrs, text) {
          return set('<tr>', attrs, text);
        },
        th: function (attrs, text) {
          return set('<th>', attrs, text);
        },
        td: function (attrs, text) {
          return set('<td>', attrs, text);
        },
        option: function (attrs, text) {
          return set('<option>', attrs, text);
        }
      };
    }

    return Object.freeze(init());
  }(),

  url: function () {
    function replaceScheme(url, scheme) {
      var idx = url.indexOf(':');
      if (idx >= 0) {
        return scheme.concat(url.slice(idx));
      } else {
        return url;
      }
    }

    function replaceHost(url, host) {
      var idx = url.indexOf('://');
      if (idx > 0) {
        var nextPort = url.indexOf(':', idx + 3);
        var nextPath = url.indexOf('/', idx + 3);
        var next = nextPort > 0 ? Math.min(nextPort, nextPath) : nextPath;
        if (next > idx) {
          return url.slice(0, idx + 3).concat(host).concat(url.slice(next));
        }
      }
      return url;
    }

    function init() {
      return {
        replaceScheme: replaceScheme,
        replaceHost: replaceHost
      };
    }

    return Object.freeze(init());
  }(),

  websocket: function (url) {
    function openWebSocket(url) {
      if ('WebSocket' in window) {
        return new WebSocket(url);
      }
    }

    function init(url) {
      var ws = openWebSocket(url);
      var supported = undefined !== ws;
      return {
        open: function (handler) {
          if (supported && 'function' === typeof handler) {
            ws.onopen = handler;
          }
          return this;
        },
        close: function (handler) {
          if (supported) {
            if ('function' === typeof handler) {
              ws.onclose = handler;
              return this;
            } else if (undefined === handler) {
              ws.close();
            }
          }
        },
        error: function (handler) {
          if (supported && 'function' === typeof handler) {
            ws.onerror = handler;
          }
          return this;
        },
        message: function (handler) {
          if (supported && 'function' === typeof handler) {
            ws.onmessage = handler;
          }
          return this;
        },
        failed: function (handler) {
          if (!supported && 'function' === typeof handler) {
            handler();
          }
          return this;
        }
      };
    }

    return Object.freeze(init(url));
  },

  speech: function (cuid, tok, options) {
    var defaultOptions = {
      lan: 'zh',
      ctp: 1,
      spd: 5,// 语速，取值0-9，默认为5中语速
      pit: 5,// 音调，取值0-9，默认为5中语调
      vol: 5,// 音量，取值0-15，默认为5中音量
      per: 0,// 发音人选择, 0为普通女声，1为普通男生，3为情感合成-度逍遥，4为情感合成-度丫丫，默认为普通女声
    };

    function init(cuid, tok, options) {
      var options = $.html.merge(options, defaultOptions);
      options.cuid = cuid;
      options.tok = tok;
      var api = 'https://tsn.baidu.com/text2audio';

      var url = api.concat('?');
      $.each(options, function (k, v) {
        url = url.concat(k).concat('=').concat(v).concat('&');
      });

      return {
        say: function (tex) {
          var promise = new Audio(url.concat('tex').concat('=').concat(tex)).play();
          if ('[object Promise]' === Object.prototype.toString.call(promise)) {
            promise.then(function () {
              $.logger.log(tex, 'glyphicon-volume-up');
            }, function (error) {
              $.logger.log(tex.concat('：').concat(error), 'glyphicon-volume-off');
            });
          }
          return this;
        }
      };
    }

    return Object.freeze(init(cuid, tok, options));
  },

  random: function () {
    var array = new Uint32Array(1);
    window.crypto.getRandomValues(array);
    return array[0];
  }
});

String.prototype.replaceAll = function (s1, s2) {
  return this.replace(new RegExp(s1, "gm"), s2);
}

Date.prototype.format = function (mask) {
  if (this.getTime() == 0) {
    return '-';
  }

  var d = this;

  if (!mask) {
    var c = new Date();
    if (c.getFullYear() == d.getFullYear() && c.getMonth() == d.getMonth() && c.getDate() == d.getDate()) {
      mask = 'HH:mm:ss';
    } else {
      mask = 'MM/dd HH:mm:ss';
    }
  }

  var zeroize = function (value, length) {
    if (!length)
      length = 2;
    value = String(value);
    for (var i = 0, zeros = ''; i < (length - value.length); i++) {
      zeros += '0';
    }
    return zeros + value;
  };

  return mask.replace(/"[^"]*"|'[^']*'|\b(?:d{1,4}|m{1,4}|yy(?:yy)?|([hHMstT])\1?|[lLZ])\b/g, function ($0) {
    switch ($0) {
      case 'd':
        return d.getDate();
      case 'dd':
        return zeroize(d.getDate());
      case 'ddd':
        return ['Sun', 'Mon', 'Tue', 'Wed', 'Thr', 'Fri', 'Sat'][d.getDay()];
      case 'dddd':
        return ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'][d.getDay()];
      case 'M':
        return d.getMonth() + 1;
      case 'MM':
        return zeroize(d.getMonth() + 1);
      case 'MMM':
        return ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'][d.getMonth()];
      case 'MMMM':
        return ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October',
          'November', 'December'][d.getMonth()];
      case 'yy':
        return String(d.getFullYear()).substr(2);
      case 'yyyy':
        return d.getFullYear();
      case 'h':
        return d.getHours() % 12 || 12;
      case 'hh':
        return zeroize(d.getHours() % 12 || 12);
      case 'H':
        return d.getHours();
      case 'HH':
        return zeroize(d.getHours());
      case 'm':
        return d.getMinutes();
      case 'mm':
        return zeroize(d.getMinutes());
      case 's':
        return d.getSeconds();
      case 'ss':
        return zeroize(d.getSeconds());
      case 'l':
        return zeroize(d.getMilliseconds(), 3);
      case 'L':
        var m = d.getMilliseconds();
        if (m > 99)
          m = Math.round(m / 10);
        return zeroize(m);
      case 'tt':
        return d.getHours() < 12 ? 'am' : 'pm';
      case 'TT':
        return d.getHours() < 12 ? 'AM' : 'PM';
      case 'Z':
        return d.toUTCString().match(/[A-Z]+$/);
        // Return quoted strings with the surrounding quotes
        // removed
      default:
        return $0.substr(1, $0.length - 2);
    }
  });
};
