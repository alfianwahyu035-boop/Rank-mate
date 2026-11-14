/* RankMate native Android file bridge (loaded by MainActivity after WebView load).
   Captures generated Blob downloads and saves them to Downloads/RankMate.
   The app also calls RankMateNativeSave for PNG sharing. */
(function(){
  'use strict';
  if(window.__rankMateNativeInstalled || !window.AndroidRankMate) return;
  window.__rankMateNativeInstalled=true;
  var CHUNK_SIZE=128*1024; // divisible by 4 for independent base64 decoding
  function sendBase64(name,mime,base64,share){
    window.AndroidRankMate.beginExport(String(name||'rankmate-file'),String(mime||'application/octet-stream'),!!share);
    for(var i=0;i<base64.length;i+=CHUNK_SIZE){
      window.AndroidRankMate.appendExport(base64.slice(i,i+CHUNK_SIZE));
    }
    window.AndroidRankMate.finishExport();
  }
  window.RankMateNativeSave=sendBase64;
  document.addEventListener('click',function(event){
    var anchor=event.target && event.target.closest && event.target.closest('a[download]');
    if(!anchor) return;
    var href=anchor.getAttribute('href')||anchor.href||'';
    if(!/^blob:|^data:/i.test(href)) return;
    event.preventDefault();
    event.stopPropagation();
    var filename=anchor.getAttribute('download')||'rankmate-export';
    fetch(href).then(function(response){if(!response.ok)throw Error('Gagal membaca berkas ekspor');return response.blob()})
    .then(function(blob){
      var reader=new FileReader();
      reader.onloadend=function(){
        try{sendBase64(filename,blob.type||'application/octet-stream',String(reader.result).split(',')[1]||'',false)}
        catch(err){console.error('Native export failed',err)}
      };
      reader.onerror=function(){console.error('Export blob reader error')};
      reader.readAsDataURL(blob);
    }).catch(function(err){console.error('Native export failed',err)});
  },true);
})();
