package com.example.shoppinglist.resource;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.shoppinglist.exception.BusinessException;
import com.example.shoppinglist.exception.BusinessExceptionReason;
import com.example.shoppinglist.resource.persistance.entity.ItemEntity;
import com.example.shoppinglist.resource.persistance.entity.ShoppingListEntity;
import com.example.shoppinglist.resource.persistance.repository.ItemEntityRepository;
import com.example.shoppinglist.resource.persistance.repository.ShoppingListRepository;
import com.example.shoppinglist.util.CustomPage;
import com.example.shoppinglist.util.CustomPagebale;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShoppingListService {

	private final ShoppingListRepository shoppingListRepository;
	private final ItemEntityRepository itemEntityRepository;
	private final ShoppingListMapper shoppingListMapper;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void createShoppingList(ShoppingListDto shoppingListDTO) {
		shoppingListRepository
				.save(ShoppingListEntity.builder().name(shoppingListDTO.name()).type(shoppingListDTO.type()).build());
	}

	@Transactional(readOnly = true)
	public CustomPage<ShoppingListDto> getShoppingList(int page, int size) {
		Page<ShoppingListEntity> shoppingListPage = shoppingListRepository.findAll(PageRequest.of(page, size));

		return CustomPage.<ShoppingListDto>builder()
				.content(shoppingListMapper.fromShoppingListEntity(shoppingListPage.getContent()))
				.pageable(CustomPagebale.builder().pageNumber(shoppingListPage.getPageable().getPageNumber())
						.pageSize(shoppingListPage.getPageable().getPageSize())
						.totalElements(shoppingListPage.getTotalElements()).build())
				.build();
	}

	@Transactional(readOnly = true)
	public ShoppingListDto getShoppingListById(Long id) {
		ShoppingListEntity shoppingListEntity = shoppingListRepository.findById(id)
				.orElseThrow(() -> new BusinessException(BusinessExceptionReason.INVALID_SHOPPING_LIST_ID));
		return shoppingListMapper.fromShoppingListEntity(shoppingListEntity);

	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
	public void updateShoppingListById(Long id, ShoppingListDto shoppingListDto) {
		ShoppingListEntity shoppingListEntity = shoppingListRepository.findById(id)
				.orElseThrow(() -> new BusinessException(BusinessExceptionReason.INVALID_SHOPPING_LIST_ID));
		shoppingListEntity.setName(shoppingListDto.name());
		shoppingListRepository.save(shoppingListEntity);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
	public void deleteShoppingListById(Long id) {
		if (!shoppingListRepository.existsById(id))
			throw new BusinessException(BusinessExceptionReason.INVALID_SHOPPING_LIST_ID);
		shoppingListRepository.deleteById(id);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
	public void additemInShoppingList(Long id, List<ItemDto> shoppingListItemDtos) {
		ShoppingListEntity shoppingListEntity = shoppingListRepository.findById(id)
				.orElseThrow(() -> new BusinessException(BusinessExceptionReason.INVALID_SHOPPING_LIST_ID));

		List<ItemEntity> entities = shoppingListMapper.fromItemDto(shoppingListItemDtos);
		entities.forEach(item -> item.setShoppingList(shoppingListEntity));
		itemEntityRepository.saveAll(entities);

	}

	@Transactional(readOnly = true)
	public CustomPage<ItemDto> getShoppingListItems(Long id, int page, int size) {
		if (!shoppingListRepository.existsById(id))
			throw new BusinessException(BusinessExceptionReason.INVALID_SHOPPING_LIST_ID);

		Page<ItemEntity> items = itemEntityRepository.findAllByShoppingListId(id, PageRequest.of(page, size));
		return CustomPage.<ItemDto>builder().content(shoppingListMapper.fromItemEntity(items.getContent()))
				.pageable(CustomPagebale.builder().pageNumber(items.getPageable().getPageNumber())
						.pageSize(items.getPageable().getPageSize()).totalElements(items.getTotalElements()).build())
				.build();
	}

	@Transactional(readOnly = true)
	public ItemDto getShoppingListItemByProductId(Long id, String productId) {
		if (!shoppingListRepository.existsById(id))
			throw new BusinessException(BusinessExceptionReason.INVALID_SHOPPING_LIST_ID);

		ItemEntity entity = itemEntityRepository.findByProductIdAndShoppingListId(productId, id)
				.orElseThrow(() -> new BusinessException(BusinessExceptionReason.INVALID_PRODUCT_ID));
		return shoppingListMapper.fromItemEntity(entity);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
	public void updateShoppingListItemsByProductId(Long id, String productId, ItemDto itemDto) {
		if (!shoppingListRepository.existsById(id))
			throw new BusinessException(BusinessExceptionReason.INVALID_SHOPPING_LIST_ID);

		ItemEntity entity = itemEntityRepository.findByProductIdAndShoppingListId(productId, id)
				.orElseThrow(() -> new BusinessException(BusinessExceptionReason.INVALID_PRODUCT_ID));
		entity.setQuantity(itemDto.quantity());
		itemEntityRepository.save(entity);
	}
}
