/* Updates the current language of the user */
function updateLanguage(lang) {
	let url = "/settings?language=" + lang;

	fetch(url, {
		method: "POST"
	})
	.then();
}