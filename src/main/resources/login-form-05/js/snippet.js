/*!
 * login-form-05 - Colorlib. No jQuery, no framework.
 * Behaviours: bs-event-classes
 */
(function () {
  'use strict';

  /* Handlers of Bootstrap's events, as the page wrote them. */
  var BSE = [{"on":".collapse","event":"show.bs.collapse","steps":[{"rel":"closest","sel":".accordion-item","op":"addClass","cls":"active","delay":0}]},{"on":".collapse","event":"hidden.bs.collapse","steps":[{"rel":"closest","sel":".accordion-item","op":"removeClass","cls":"active","delay":0}]}], BSE_INIT = [{"sel":".btn-link[aria-expanded=\"true\"]","closest":".accordion-item","cls":"active"}], BSE_NAV = null;
  BSE_INIT.forEach(function (x) { document.querySelectorAll(x.sel).forEach(function (el) { var t = el.closest(x.closest); if (t) t.classList.add(x.cls); }); });
  BSE.forEach(function (h) {
    document.querySelectorAll(h.on).forEach(function (el) {
      el.addEventListener(h.event, function (e) {
        if (e.target !== el) return;               // only this element's own event, as jQuery's delegation-free .on() had it
        h.steps.forEach(function (s) {
          var run = function () {
            var targets = s.rel === 'closest' ? [el.closest(s.sel)] : [].slice.call(el.querySelectorAll(s.sel));
            targets.forEach(function (t) { if (t) t.classList[s.op === 'addClass' ? 'add' : 'remove'](s.cls); });
          };
          if (s.delay) setTimeout(run, s.delay); else run();
        });
      });
    });
  });
  if (BSE_NAV) document.querySelectorAll(BSE_NAV).forEach(function (a) {
    a.addEventListener('click', function () { if (a.href && a.getAttribute('href') !== '#') location.href = a.href; });
  });
})();
