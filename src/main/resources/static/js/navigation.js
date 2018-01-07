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
            var a = $("<a style='display: none;'/>");
            var url = window.URL.createObjectURL(new Blob([result], {type: "data:attachment/text"}));
            a.attr("href", url);
            a.attr("download", "report.txt");
            $("body").append(a);
            a[0].click();
            window.URL.revokeObjectURL(url);
            a.remove();
        },
        error: function() {
            alert("something went wrong :(");
        },

    });
}