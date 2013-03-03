(function($){ 

    // cache the template regular expression
    var templateRE = /\{([a-z\_\-0-9]+)\}/gi;

    // utility methods
    var objReplace = function(s, o, prune) {
        return s.replace(templateRE, function($0, $1, index) {
                             if (o[$1] === null) {
                                 return "";
                             } else {
                                 ret = o[$1];
                                 if (prune) {
                                     delete(o[$1]);
                                 };
                                 return ret;
                             }
                         });
    };

    var replaceURIParams = function(s, v) {
        return s.replace(templateRE, v);
    };


    $.etsy = {
        etsyURL: "http://beta-api.etsy.com/v1{command}.js?",
    
        methods: [
            {"name":"getUserDetails","uri":"\/users\/{user_id}"},
            {"name":"getFavorersOfUser","uri":"\/users\/{user_id}\/favorers"},
            {"name":"getFavorersOfListing","uri":"\/listings\/{listing_id}\/favorers"},
            {"name":"getUsersByName","uri":"\/users\/keywords\/{search_name}"},
            {"name":"getShopDetails","uri":"\/shops\/{user_id}"},
            {"name":"getFeaturedSellers","uri":"\/shops\/featured"},
            {"name":"getShopsByName","uri":"\/shops\/keywords\/{search_name}"},
            {"name":"getFavoriteShopsOfUser","uri":"\/users\/{user_id}\/favorites\/shops"},
            {"name":"getListingDetails","uri":"\/listings\/{listing_id}"},
            {"name":"getListings","uri":"\/shops\/{user_id}\/listings"},
            {"name":"getFeaturedDetails","uri":"\/shops\/{user_id}\/listings\/featured"},
            {"name":"getFrontFeaturedListings","uri":"\/listings\/featured\/front"},
            {"name":"getFavoriteListingsOfUser","uri":"\/users\/{user_id}\/favorites\/listings"},
            {"name":"getGiftGuideListings","uri":"\/gift-guides\/{guide_id}\/listings"},
            {"name":"getListingsByKeyword","uri":"\/listings\/keywords\/{search_terms}"},
            {"name":"getListingsByTags","uri":"\/listings\/tags\/{tags}"},
            {"name":"getChildTags","uri":"\/tags\/{tag}\/children"},
            {"name":"getTopTags","uri":"\/tags\/top"},
            {"name":"getGiftGuides","uri":"\/gift-guides"},
            {"name":"getMethodTable","uri":"\/"},
            {"name":"getServerEpoch","uri":"\/server\/epoch"},
            {"name":"ping","uri":"\/server\/ping"}
        ],

        init: function(key, options) {
            this.options = options || {};
            this.params  = {};
            if (key) this.params['api_key'] = key;
            this.initMethods(); 
        },

        /**
         * initMethods:
         * Dynamically generates methods from the method list.
         */
        initMethods: function() {
            for (var i=0, method; method = this.methods[i]; i++) {

                (function(method) {
                     $.etsy[method.name] = function() {
                         // prepend command and append a error handler.
                         var args = $.makeArray(arguments);
                         args.unshift(method.uri);
                         args.push( ($.etsy.options.error || function() {}) );

                         $.etsy.get.apply($.etsy, args);
                     };
                 })(method);

            }
        },

        getEtsyUrl: function(command, params) {
            var etsyURL = objReplace(this.etsyURL, {command: command});
            var get_params = {};

            $.each(params, function(name, value) {
                if (value) {
                    var pattern = new RegExp('{'+name+'}');
                    if (etsyURL.match(pattern)) {
                        etsyURL = etsyURL.replace(pattern, value);
                    } else {
                        get_params[name] = value;
                    }
                }
            });
            
            etsyURL = etsyURL + $.param($.extend(get_params, this.params));
            
            return etsyURL;
        },

        get: function (command, params, callback, error) {
            // shift if we didn't pass extra params
            if ($.isFunction(params)) {
                callback = params, params = {};
            }
            
            var etsyURL = this.getEtsyUrl(command, params);
        
            if (!callback) {
                callback = function() {};
            }

            if (!error) {
                error = function() {};
            }
            
            return $.ajax({
                url: etsyURL,
                dataType: 'jsonp',
                success: function(data) {
                    if (data.ok) {
                        callback.call(this, data);
                    } else {
                        error.call(this, data);
                    }
                }
            });
        }
    };
        
})(jQuery);
