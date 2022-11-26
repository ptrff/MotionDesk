var animationFrame;
var r = 10;
var g = 50;
var b = 10;
var tomax = 1;


function makeid(length) {
    var result           = '';
    var characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var charactersLength = characters.length;
    for ( var i = 0; i < length; i++ ) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}

a = document.getElementById('text');

function frame() {
  animationFrame = requestAnimationFrame(frame);

  a.innerHTML = makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18)
  +makeid(19)+"&lt;PTRFF&gt;"+makeid(18)+makeid(19)+"&lt;PTRFF&gt;"+makeid(18);

  if(tomax == 1){
   g++;
   if(g==90) tomax = 0;
  }else{
   g--;
   if(g==50) tomax = 1;
  }


  a.style.color = "#"+r+g+b;


  window.androidWallpaperInterface.drawFrame();
}

function pauseWallpaper() {
  cancelAnimationFrame(animationFrame);
}

function resumeWallpaper() {
  frame();
}

frame();

