CREATE TABLE `customer` (
 `id` bigint(20) NOT NULL,
 `first_name` varchar(255) DEFAULT NULL,
 `last_name` varchar(255) DEFAULT NULL,
 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `customer` (`id`, `first_name`, `last_name`) VALUES ('2', 'Alex', 'Gusta');