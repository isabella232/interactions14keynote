(function (ui) {
	var closePopup = ui.closePopup.bind(ui);

	document.getElementById('1btnPopup-cancel').addEventListener('click', closePopup, false);
	document.getElementById('2btnPopup-cancel').addEventListener('click', closePopup, false);
	document.getElementById('2btnPopup-ok').addEventListener('click', closePopup, false);
	document.getElementById('radioPopup-close').addEventListener('click', closePopup, false);
	document.getElementById('checkPopup-close').addEventListener('click', closePopup, false);
	document.getElementById('listPopup-cancel').addEventListener('click', closePopup, false);
	document.getElementById('listNoTitlePopup-cancel').addEventListener('click', closePopup, false);
	document.querySelector('#listNoBtnPopup .ui-listview').addEventListener('click', closePopup, false);
	document.querySelector('#listNoTitleNoBtnPopup .ui-listview').addEventListener('click', closePopup, false);
})(window.tau);
