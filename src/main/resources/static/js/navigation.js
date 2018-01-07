function popup(id1,id2) {
    window.open("/TreeCmp/trees/?firstTreeId="+id1+"&secondTreeId="+id2, 'window', 'width=1500,height=800,location=no,resizable=no,scrollbars=no,menubar=no,toolbar=no,titlebar=no,status=no');
}

function getReport(reportStstus) {
    $.ajax({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        url: "/TreeCmp/rawReport",
        type: 'POST',
        mimeType:"text/html; charset=UTF-8",
        success: function(result) {
            setTimeout(function(){
                var link = document.createElement('a');
                link.download = 'report.txt';
                var blob = new Blob([result], {type: 'text/plain'});
                link.href = window.URL.createObjectURL(blob);
                link.onclick = destroyClickedElement;
                link.click();
                reportStstus.unsaved = false;
            }, 100);
        },
        error: function() {
            alert("something went wrong :(");
        }
    });
}

function destroyClickedElement(event) {
    document.body.removeChild(event.target);
}