var animationFrame;
function makeid(length) {
    var result           = '';
    var characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var charactersLength = characters.length;
    for ( var i = 0; i < length; i++ ) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}

function a(){
button.style.color = "#FF0000";
}

button = document.getElementById('test')
button.addEventListener("click", a);
function frame() {
  animationFrame = requestAnimationFrame(frame);



  // Let the host app know
  window.androidWallpaperInterface.drawFrame();
}

function pauseWallpaper() {
  cancelAnimationFrame(animationFrame);
}

function resumeWallpaper() {
  frame();
}

frame();

