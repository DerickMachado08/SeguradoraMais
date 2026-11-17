/* Movimento leve da experiência de produto, com alternativa estática para
   preferências de redução de movimento. */
(function () {
    const cards = [...document.querySelectorAll('.seguro-produto-card')];
    const gsap = window.gsap;
    if (!cards.length || !gsap || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

    gsap.from(cards, {
        y: 22,
        opacity: 0,
        duration: .6,
        stagger: .08,
        ease: 'power3.out',
        clearProps: 'transform,opacity'
    });

    if (!window.matchMedia('(pointer: fine)').matches) return;

    cards.forEach((card) => {
        const visual = card.querySelector('.seguro-product-visual');
        const shield = card.querySelector('.seguro-product-shield');
        const floor = card.querySelector('.seguro-product-floor');
        if (!visual || !shield || !floor) return;

        const rotateY = gsap.quickTo(shield, 'rotationY', { duration: .5, ease: 'power3.out' });
        const rotateX = gsap.quickTo(shield, 'rotationX', { duration: .5, ease: 'power3.out' });
        const shadowX = gsap.quickTo(floor, 'x', { duration: .6, ease: 'power3.out' });

        visual.addEventListener('pointermove', (event) => {
            const rect = visual.getBoundingClientRect();
            const x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
            const y = ((event.clientY - rect.top) / rect.height) * 2 - 1;
            rotateY(x * 11);
            rotateX(y * -7);
            shadowX(x * -13);
        });
        visual.addEventListener('pointerleave', () => {
            rotateY(0);
            rotateX(0);
            shadowX(0);
        });
    });
})();
