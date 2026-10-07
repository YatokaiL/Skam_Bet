// live recalculation of the potential win in the bet slip
(function () {
    var amountInput = document.getElementById('amount');
    var oddsEl = document.getElementById('coupon-odds');
    var winEl = document.getElementById('potential-win');
    if (!amountInput || !oddsEl || !winEl) {
        return;
    }

    var odds = parseFloat(oddsEl.getAttribute('data-odds'));

    function recalc() {
        var amount = parseFloat(amountInput.value.replace(',', '.'));
        var win = (isNaN(amount) || amount <= 0) ? 0 : amount * odds;
        winEl.textContent = win.toFixed(2) + ' ₽';
    }

    amountInput.addEventListener('input', recalc);
    recalc();
})();
