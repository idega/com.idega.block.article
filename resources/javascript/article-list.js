jQuery.noConflict();

jQuery(document).ready(function() {
	jQuery('.content_list .article_item .article_teaser').hide();
	jQuery('.content_list .article_item').prepend('<div class="teaserExpander"></div>');
	jQuery('.content_item_toolbar_topcontainer').remove().appendTo('.articleListButtons');

	jQuery('.content_list .article_item .teaserExpander').toggle(
        function(){
            jQuery(this).addClass('expanded');
            jQuery(this).parent().find('.article_teaser').slideDown('fast');
        },
        function(){
            jQuery(this).removeClass("expanded");
            jQuery(this).parent().find('.article_teaser').slideUp('fast');
        }
    );
});