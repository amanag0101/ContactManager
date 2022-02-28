$(document).ready(() => {
	
});

const toggleSidebar = () => {
    const sidebar = document.getElementById("sidenav");
    const hamburger = document.getElementById("hamburger");
    
    if(sidebar.style.display == "none") {
        sidebar.style.display = "block";
        hamburger.style.display = "none";
    }
    else {
        sidebar.style.display = "none";
        hamburger.style.display = "block";
    }
}