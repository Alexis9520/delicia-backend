package com.delicia.deliciabackend.dto;

import com.delicia.deliciabackend.model.Order;
import com.delicia.deliciabackend.model.OrderItem;
import com.delicia.deliciabackend.model.Address;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {
    public static OrderResponse toResponse(Order order) {
        OrderResponse dto = new OrderResponse();

        dto.setId(order.getId());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setTotal(order.getTotal());
        dto.setPaymentIntentId(order.getPaymentIntentId());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());

        // Address
        Address addr = order.getAddress();
        if (addr != null) {
            AddressResponse addrDto = new AddressResponse();
            addrDto.setStreet(addr.getStreet());
            addrDto.setCity(addr.getCity());
            addrDto.setPostalCode(addr.getPostalCode());
            addrDto.setCountry(addr.getCountry());
            addrDto.setPhone(addr.getPhone());
            dto.setAddress(addrDto);
        }

        // Items
        List<OrderItemResponse> itemDtos = order.getItems().stream().map(item -> {
            OrderItemResponse itemDto = new OrderItemResponse();
            itemDto.setId(item.getId());
            itemDto.setName(item.getProduct().getName());
            itemDto.setPrice(item.getProduct().getPrice());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setImage(item.getProduct().getImage());
            return itemDto;
        }).collect(Collectors.toList());
        dto.setItems(itemDtos);

        // NUEVO: Campos para ventas mostrador
        dto.setCanal(order.getCanal());
        dto.setNombreCliente(order.getNombreCliente());
        dto.setDocumentoCliente(order.getDocumentoCliente());

        return dto;
    }
}