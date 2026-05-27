
    function irLogin(perfil){
    localStorage.setItem("perfil", perfil);
    window.location.href = "/login.html";
}

    /* ANIMAÇÃO */

    const sections = document.querySelectorAll('.section');
    const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if(entry.isIntersecting){
            entry.target.classList.add('show');
        }
    });

}, {
    threshold: 0.2
});

    sections.forEach(sec => observer.observe(sec));

    /* MENU MOBILE */

    const menu = document.querySelector(".menu");
    const btn = document.querySelector(".menu-mobile-btn");
    btn.addEventListener("click", () => {
    menu.classList.toggle("active");

});

    /* CARROSSEL */

    const slides = document.querySelectorAll(".slide");
    const dots = document.querySelectorAll(".dot");

    let index = 0;

    function trocarSlide(){

    slides[index].classList.remove("active");
    dots[index].classList.remove("active");
    index++;
    if(index >= slides.length){
    index = 0;

}
    slides[index].classList.add("active");
    dots[index].classList.add("active");

}

    const carousel = document.querySelector(".carousel");
    const nextBtn = document.querySelector(".next");
    const prevBtn = document.querySelector(".prev");

    nextBtn.addEventListener("click", () => {
        carousel.scrollBy({
            left: 320,
            behavior: "smooth"
        });
    });

    prevBtn.addEventListener("click", () => {
        carousel.scrollBy({
            left: -320,
            behavior: "smooth"
        });
    });

    setInterval(trocarSlide, 5000);
