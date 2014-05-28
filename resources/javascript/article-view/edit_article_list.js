(function($) {
		var PH_DATA_KEY = "ph-product-list-data";
		var showContentFound = function(main){
			main.find('.list-content').show();
			main.find('.not-found-content').hide();
		}
		var showContentNotFound = function(main){
			main.find('.list-content').hide();
			main.find('.not-found-content').show();
		}
		var deleteCallback = function(reply,data){
			if(reply.status != "OK"){
				// Actions for failure
				closeAllLoadingMessages();
				humanMsg.displayMsg(reply.message);
				return;
			}
			data.input.parents('.item-element').first().remove();
			if(reply.message){
				humanMsg.displayMsg(reply.message);
			}
			closeAllLoadingMessages();
			return;
	   }
		var fbOptions = {
				type: 'iframe',
				beforeLoad:function(){
					showLoadingMessage('');
				},
				afterShow:function(){
					jQuery('.fancybox-iframe').contents().find('.groupsChooserBoxBodyStyle').parents('.input-div-container').first().hide();
					closeAllLoadingMessages();
				},
				width:jQuery(window).width() * 0.8,
				height:jQuery(window).width() * 0.8
		};
		var addActions = function(row,list){
			row.find('.remove').click(function(e){
				e.preventDefault();
				var input = jQuery(this);
				var id = input.find('.id-input').val();
				ArticleServices.deleteArticle(id,{
					callback : deleteCallback,
					arg : {input : input}
			   });
			});
			var opts = jQuery.extend({},fbOptions,{beforeClose : function(){
				search(null,list.parents('.search-results').first());
			}});
			row.find('.edit-button').fancybox(opts);
			row.find('.remove, .edit-button').each(function(){
				var btn = jQuery(this);
				btn.tooltip({ 
						tooltipClass: "controlls-tooltip"
						,track:false
						,position:{my:"left top+25%",at:"left bottom",collision:"flipfit"}
						,show:false
						,hide:false
				});
			});
		}
		var displayList = function(list,products,data){
			list.empty();
			if(!products){
				return;
			}
			var listItem = list.parent().find('.components-div').children('.item-element');;
			for(var i = 0; i < products.length;i++){
				var product = products[i];
				var item = listItem.clone();
				var title = item.find('.product-title');
				title.append(product.title);
				
				var edit = item.find('.edit-button');
				edit.attr('href',product.editUri);
				
				var idInput = item.find('.id-input');
				idInput.val(product.id);
				
				addActions(item,list);
				list.append(item);
				
			}
		}
		var searchCallback = function(reply,data){
			if(reply.status != "OK"){
				// Actions for failure
				closeAllLoadingMessages();
				humanMsg.displayMsg(reply.message);
				return;
			}
			data.totalCount = reply.totalCount;
			displayList(jQuery(data.main).find('.items-list'),reply.articles,data);
			createPagesNavigation(data,reply.pages);
			data.afterShow(data);
			
			closeAllLoadingMessages();
			return;
	   }
		var search = function(term,list){
			   showLoadingMessage(EditArticleListHelper.messages.loading);
			   var data = list.data(PH_DATA_KEY);
			   data.searchTerm = term;
			   ArticleServices.getArticles(data.maxResult,data.startPosition,{
					callback : searchCallback,
					arg : data
			   });
		}
		var newSearch = function(term,list){
				var data = list.data(PH_DATA_KEY);
				data.startPosition = 0;
			   search(term,list);
		}
		var createPagesNavigation = function(data,pages){
			var main = jQuery(data.main);
			main.find('.pages-navigation').each(function(){
				var navigation = jQuery(this);
				navigation.find('.page-select').remove();
				var hasPrev = false;
				var hasNext = false;
				var foundActive = false;
				for(var i = 0;i < pages.length;i++){
					var page = pages[i];
					var link = main.find('.components-div').find('.page-select').clone();
					link.find('.page-number').text(page.number);
					link.find('.result-start-position').val(page.start);
					if(!foundActive && (page.start >= data.startPosition) && (page.start < (data.startPosition + data.maxResult))){
						link.addClass("active");
						foundActive = true;
						if(i > 0){
							hasPrev = true;
						}
					}else{
						if(foundActive){
							hasNext = true;
						}
					}
					if(hasPrev){
						navigation.find('.prev').show();
					}else{
						navigation.find('.prev').hide();
					}
					if(hasNext){
						navigation.find('.next').show();
					}else{
						navigation.find('.next').hide();
					}
					link.click({main: data.main},function(e){
						e.preventDefault();
						if(jQuery(this).hasClass('active')){
							return;
						}
						var main = jQuery(e.data.main);
						var data = main.data(PH_DATA_KEY);
						data.startPosition = jQuery(this).find('.result-start-position').val();
						search(data.searchTerm,main);
					});
					navigation.append(link);
				}
				var pagesInfo = main.find('.pages-info');
				var startNum = Number(data.startPosition);
				pagesInfo.find('.first-result-number').text(1 + startNum);
				pagesInfo.find('.last-result-number').text(startNum + main.find('.items-list').children('.item-element').length);
			});
		}
		var createNavigation = function(data){
			var main = jQuery(data.main);
			main.find('.pages-navigation').each(function(){
				var navigation = jQuery(this);
				navigation.find('.prev').click(function(e){
					jQuery(this).parent().find('.active').prev().click();
					e.preventDefault();
				});
				navigation.find('.next').click(function(e){
					jQuery(this).parent().find('.active').next().click();
					e.preventDefault();
				});
			});
			main.find('[name="max-result"]').change(data,function(e){
				var main = jQuery(e.data.main);
				var data = main.data(PH_DATA_KEY);
				data.maxResult = jQuery(this).val();
				data.startPosition = 0;
				main.find('[name="max-result"]').val(data.maxResult);
				search(data.searchTerm,main);
			});
		}
		var countItems = function(list){
			return jQuery(list).find('.items-list').children().length;
		}
		var create = function(options,list){
			defaults = {
					   maxResult : null,
					   startPosition : null,
					   afterShow : function(){}
			};
			list.find('.item-element').each(function(){
				addActions(jQuery(this),list);
			});
			var fOpts = jQuery.extend({},fbOptions,{beforeClose : function(){
				search(null,list);
			}});
			list.find('.create-article-btn').fancybox(fOpts);
			var opts = $.extend({}, defaults, options);
			list.data(PH_DATA_KEY,opts)
			var links = list.find('.pages-navigation').first().find('.page-select');
			var pages = [];
			for(var i = 0;i < links.length;i++){
				var page = {};
				var link = jQuery(links[i]);
				page.start = link.find('.result-start-position').val();
				page.number = link.find('.page-number').text();
				pages.push(page);
			}
			createPagesNavigation(options,pages);
			createNavigation(options);
			if(countItems(list) < 1){
				showContentNotFound(list);
			}else{
				showContentFound(list);
			}
		}
	   $.fn.editArticlesList = function(action,data) {
		   if(action == 'countListItems'){
			   return countItems(this);
		   }
		   return this.each(function(){
			   if(action == 'search'){
				   newSearch(data,jQuery(this));
				   return;
			   }
			   create(action,jQuery(this));
		   });
	   }
})(jQuery);

var EditArticleListHelper = {};
EditArticleListHelper.messages = {};
