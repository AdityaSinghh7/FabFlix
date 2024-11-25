document.addEventListener('DOMContentLoaded', function() {

    function toggleDarkMode() {
        document.body.classList.toggle('dark-mode');
        const logo = document.querySelector('.fabflix-logo');
        if (document.body.classList.contains('dark-mode')) {
            logo.src = "../Images/FabFlix-darkMode.svg";
            localStorage.setItem('theme', 'dark');
        } else {
            logo.src = "../Images/FabFlix-lightMode.svg";
            localStorage.setItem('theme', 'light');
        }
    }
    document.getElementById('dark-mode-toggle').addEventListener('click', toggleDarkMode);


    (function() {
        const savedTheme = localStorage.getItem('theme');
        const logo = document.querySelector('.fabflix-logo');
        if (savedTheme === 'dark') {
            document.body.classList.add('dark-mode');
            logo.src = "../Images/FabFlix-darkMode.svg";
        }
    })();
});