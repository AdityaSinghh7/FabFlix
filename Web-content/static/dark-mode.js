document.addEventListener('DOMContentLoaded', function() {

    function toggleDarkMode() {
        document.body.classList.toggle('dark-mode');
        // Save the preference to localStorage
        if (document.body.classList.contains('dark-mode')) {
            localStorage.setItem('theme', 'dark');
        } else {
            localStorage.setItem('theme', 'light');
        }
    }
    document.getElementById('dark-mode-toggle').addEventListener('click', toggleDarkMode);


    (function() {
        const savedTheme = localStorage.getItem('theme');
        if (savedTheme === 'dark') {
            document.body.classList.add('dark-mode');
        }
    })();
});