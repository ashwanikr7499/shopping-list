package com.example.shoppinglist.resource;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonView;

public record ShoppingListItemDto(

        // @formatter:off
        @JsonView( {
                RequestView.ShoppingListAddItem.class,
                ResponseView.ShoppingListItem.class }) @NotBlank(groups = {
                        RequestView.ShoppingListAddItem.class }) String productId,

        @JsonView({ RequestView.ShoppingListAddItem.class, RequestView.ShoppingListUpdateItem.class,
                ResponseView.ShoppingListItem.class }) @Min(value = 0, groups = { RequestView.ShoppingListAddItem.class,
                        RequestView.ShoppingListUpdateItem.class }) int qty
    // @formatter:on
    ){
}
