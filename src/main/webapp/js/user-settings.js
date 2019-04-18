/* Updates the current language of the user */
function updateLanguage(code, lang, index) {
	let url = "/settings?language=" + code;

	fetch(url, {
		method: "POST"
	})
	.then((response) => {
		return response.text();
	})
	.then((text) => {
		var languages = document.getElementsByClassName("lang");

		for (var i=0; i < languages.length; i++) {
			if (i == index) {
				languages[i].style.background = '#9ACD32';
			} else {
				languages[i].style.background = '';
			}
		}
	})
}