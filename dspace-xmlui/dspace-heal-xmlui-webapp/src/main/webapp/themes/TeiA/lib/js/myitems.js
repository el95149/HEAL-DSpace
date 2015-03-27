
function drawFavorites(favBtn, initVal, labels) {

    favBtn.button('reset');
    favBtn.attr('data-on', initVal.toString());
    var iconSpan = favBtn.find('span');

    if (initVal) {
        favBtn.addClass('on');
        //iconSpan.removeClass('glyphicon-star').addClass('glyphicon-ok');
    } else {
        favBtn.removeClass('on');
        //iconSpan.removeClass('glyphicon-ok').addClass('glyphicon-star');
    }

}

function drawAuthored(authoredBtn, authoredInfo, authoredModalBtn, initVal, labels) {

    authoredBtn.button('reset');
    authoredBtn.attr('data-on', initVal.toString());
    var iconSpan = authoredBtn.find('span');

    if (initVal) {
        authoredBtn.html(labels.remove);
        authoredInfo.html(labels.isAuthored + " ");
        authoredModalBtn.html(labels.removeModal);
        authoredModalBtn.removeClass('btn-sm').addClass('btn-xs');
    } else {
        authoredBtn.html(labels.add);
        authoredInfo.html("");
        authoredModalBtn.html(labels.addModal);
        authoredModalBtn.removeClass('btn-xs').addClass('btn-sm');
    }

}

function setupFavoritesActions(favBtn, actionUrl) {

    //var removeText = favBtn.data('remove');
    //var curContent = favBtn.html();

    favBtn.on({
        click: function(e) {
            favBtn.button('loading');

            $.post(
                actionUrl,
                { toggle: true, listType: 'favorite' },
                function(msg) {
                    var initVal = msg.toString() === 'true';
                    drawFavorites(favBtn, initVal);
                    //curContent = favBtn.html();
                }
            ).always(function() {
                favBtn.button('reset');
            });

            return false;
        },
        mouseenter: function(e) {
            if (favBtn.attr('data-on').toString() === 'true') {
                favBtn.find('span').removeClass('glyphicon-star').addClass('glyphicon-minus-sign');
            }
        },
        mouseleave: function(e) {
            if (favBtn.attr('data-on').toString() === 'true') {
                favBtn.find('span').removeClass('glyphicon-minus-sign').addClass('glyphicon-star');
            }
        }
    });

}

function setupAuthoredActions(authoredBtn, authoredInfo, authoredModalBtn, actionUrl, labels) {

    authoredBtn.on({
        click: function(e) {
            authoredBtn.button('loading');

            $.post(
                actionUrl,
                { toggle: true, listType: 'authored' },
                function(msg) {
                    var initVal = msg.toString() === 'true';
                    drawAuthored(authoredBtn, authoredInfo, authoredModalBtn, initVal, labels);
                    //$('#authModal').modal('hide')
                    window.location.reload();
                }
            ).always(function() {
                authoredBtn.button('reset');
                //$('#authModal').modal('hide');
                window.location.reload();
            });

            return false;
        }
    });


}

function initMyItems(configParams) {

    var favBtn = $('#'+configParams.favoriteBtnId);
    var authoredBtn = $('#'+configParams.authoredBtnId);
    var authoredInfo = $('#'+configParams.authoredInfoId);
    var authoredModalBtn = $('#'+configParams.authoredModalBtnId);
    var itemId = configParams.itemId;
    var context = configParams.context;
    var labels = configParams.labels;

    var myItemsHandlerUrl = context + '/myir/my-items/handler/' + itemId ;

    var initFavorite = false;
    var initAuthored = false;

    $.getJSON(myItemsHandlerUrl, function(resp) {
        console.log(resp);
        initFavorite = resp.favorite.toString() === 'true';
        initAuthored = resp.authored.toString() === 'true';
        drawFavorites(favBtn, initFavorite, labels);
        drawAuthored(authoredBtn, authoredInfo, authoredModalBtn, initAuthored, labels);
    }).fail(function( jqxhr, textStatus, error ) {
        console.log( "Request Failed: " + textStatus + ", " + error );
    });

    setupFavoritesActions(favBtn, myItemsHandlerUrl);
    setupAuthoredActions(authoredBtn, authoredInfo, authoredModalBtn, myItemsHandlerUrl, labels);

}