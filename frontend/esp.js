;(function($) {

      this.Etsy = this.Etsy || {};

      Etsy.ESP = function(elem, options) {
          $.etsy.init("59v7he5jmht7825xg6ektjgp");

          this.board = $(elem);
          this.initBoard();

          var self = this;
          var bind = function(ev, fn) {
              $(elem).bind(ev, function(event, data) {
                               self[fn].apply(self, [data.data]);
                           });
          };

          $.comet.init('/etsyesp/cometd/cometd');
          $.comet.subscribe('/esp');
          bind('/esp/tag', 'opponent_tag');
          bind('/esp/newgame', 'start');
          bind('/esp/gameover', 'done');
          bind('/esp/newlisting', 'newListing');
          bind('/esp/rejoin', 'rejoin');
      };


      Etsy.ESP.prototype.opponent_tag = function(data) {
          var s = '';
          for(var i = 0; i < data.taglength; i++) {
              s += '*';
          }
          this.tag($('.theirs'), s);
      };

      Etsy.ESP.prototype.tag = function(div, tag) {
          div.append('<p class="tag">'+tag+'</p>');
      };


      Etsy.ESP.prototype.initBoard = function() {
          this.board.append(
              '<div class="join-hint">Type your Etsy username:<div> \
               <form action="#"> \
                <input class="username" type="text"/> \
                <button class="join">Join Game</button> \
               </form>'
          );

          var self = this;
          $("#board .join").bind(
              'click',
              function() {
                  self.join();
                  return false;
              });
      };


      Etsy.ESP.prototype.join = function() {
          this.name = $("#board .username").val();
          this.rejoin();
      };


      Etsy.ESP.prototype.rejoin = function() {
          $.comet.publish('/esp', { connect: true, name: this.name });
          $.comet.publish('/esp/join', {});
          this.signalWaiting();
      };


      Etsy.ESP.prototype.signalWaiting = function() {
          this.board.html('Waiting for another player...');
      };


      Etsy.ESP.prototype.start = function(data) {
          this.board.html(
              '<div class="controls"> \
                <div class="tags"> \
                  <div class="mine" /><div class="theirs" /> \
                </div> \
                <form id="guessform" action="#"> \
                  <input type="text" id="tag" /> \
                  <button class="guess">Guess!</button> \
                </form> \
                <div class="opponent"> \
                  <div class="label">Currently playing:</div> \
                  <div class="avatar"><img src="' + data.opponent + '" /></div> \
                </div> \
               </div> \
               <div class="listing"></div> \
          ');
          this.tags = [];

          var self = this;
          var tagit = function() {
              var box = $('#tag');
              var tag = box.val();
              if(tag) {
                  self.tag($('.mine'), tag);
                  self.tags.push(tag);
                  $.comet.publish('/esp/tag', { tag: tag });
                  box.val('');
                  box.focus();
              }
              return false;
          };
          $('#board .guess').bind('click', tagit);
          $('#guessform').bind('submit', tagit);
      };


      Etsy.ESP.prototype.newListing = function(data) {
          var self = this;
          if(data.opponent_guesses && data.opponent_guesses.length > 0) {
              this.updateGuesses(data.opponent_guesses);
          }
          $.etsy.getListingDetails(
              { listing_id: data.listing_id,
                detail_level: 'high' },
              function(response) {
                  var url = response.results[0].image_url_200x200;
                  $("#board .listing").html('<img src="' + url + '" />');
              });
      };


      Etsy.ESP.prototype.updateGuesses = function(guesses) {
          $('.mine > .tag').each(
              function() {
                  $(this).addClass('remove');
              });

          var hidden = $('.theirs > .tag:not(.remove)');
          for(var i = 0; i < Math.min(hidden.length, guesses.length); i++) {
              var h = $(hidden[i]);
              h.text(guesses[i]);
              h.addClass('remove');
          }
          setTimeout(function() {$('.remove').remove();}, 1500);
      };


      Etsy.ESP.prototype.done = function(data) {
      };


      $.fn.esp = function(options) {
          return this.each(
              function() {
                  return (
                      $.data(this, "ESP") ||
                      $.data(this, "ESP", new Etsy.ESP(this, options))
                  );
              });
      };

})(jQuery);