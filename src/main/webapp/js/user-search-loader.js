void function () {
  'use strict';

  function getNames(callback) {
    fetch("/geochart")
    .then((response) => {
      return response.json();
    })
    .then((usrJson) => {
      var names = [];
      for (var i = 0; i < usrJson.length; i++) {
        names.push(usrJson[i].name);
        names.push(usrJson[i].email);
      }
      callback(names);
    });
  }

  getNames(function(names) {

    horsey(document.querySelector('#hy'), {
      source: [{ list: names }]
    });


    function events (el, type, fn) {
      if (el.addEventListener) {
        el.addEventListener(type, fn);
      } else if (el.attachEvent) {
        el.attachEvent('on' + type, wrap(fn));
      } else {
        el['on' + type] = wrap(fn);
      }
      function wrap (originalEvent) {
        var e = originalEvent || global.event;
        e.target = e.target || e.srcElement;
        e.preventDefault  = e.preventDefault  || function preventDefault () { e.returnValue = false; };
        e.stopPropagation = e.stopPropagation || function stopPropagation () { e.cancelBubble = true; };
        fn.call(el, e);
      }
    }

  });

  
}();
