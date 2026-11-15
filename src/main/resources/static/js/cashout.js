// feature 18: cashout amounts follow the odds - asked from the server every 2 seconds without reloading the page
(function () {
    var script = document.currentScript;
    var url = script.getAttribute('data-url');
    if (!document.querySelector('.cashout-form')) {
        return;
    }

    function update(offers) {
        document.querySelectorAll('.cashout-form').forEach(function (form) {
            var offer = offers[form.getAttribute('data-bet-id')];
            if (offer === undefined) {
                // the event has started or the bet is not in play anymore
                var cell = form.parentNode;
                form.remove();
                var none = document.createElement('span');
                none.className = 'cashout-none';
                none.textContent = '\u2014';
                cell.appendChild(none);
                return;
            }
            form.querySelector('input[name=expectedAmount]').value = offer;
            form.querySelector('.cashout-amount').textContent = offer + ' \u20BD';
        });
    }

    function refresh() {
        fetch(url, {headers: {'Accept': 'application/json'}, credentials: 'same-origin'})
            .then(function (response) {
                return response.ok ? response.json() : null;
            })
            .then(function (offers) {
                if (offers) {
                    update(offers);
                }
            })
            .catch(function () {
                // the session has expired or the server is not available - try again later
            });
    }

    setInterval(refresh, 2000);
})();
