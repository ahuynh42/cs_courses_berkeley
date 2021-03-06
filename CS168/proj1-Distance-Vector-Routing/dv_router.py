"""
Your awesome Distance Vector router for CS 168
"""

import sim.api as api
import sim.basics as basics

from dv_utils import PeerTable, PeerTableEntry, ForwardingTable, \
	ForwardingTableEntry

# We define infinity as a distance of 16.
INFINITY = 16

# A route should time out after at least 15 seconds.
ROUTE_TTL = 15


class DVRouter(basics.DVRouterBase):
	# NO_LOG = True  # Set to True on an instance to disable its logging.
	# POISON_MODE = True  # Can override POISON_MODE here.
	# DEFAULT_TIMER_INTERVAL = 5  # Can override this yourself for testing.

	def __init__(self):
		"""
		Called when the instance is initialized.

		DO NOT remove any existing code from this method.
		"""
		self.start_timer()  # Starts calling handle_timer() at correct rate.

		# Maps a port to the latency of the link coming out of that port.
		self.link_latency = {}

		# Maps a port to the PeerTable for that port.
		# Contains an entry for each port whose link is up, and no entries
		# for any other ports.
		self.peer_tables = {}

		# Forwarding table for this router (constructed from peer tables).
		self.forwarding_table = ForwardingTable()

		# SELF ADDED: A "history" data structure that records the latest 
		# route advertisement sent out of each port for each destination host.
		self.history = {}

		# SELF ADDED: Copy of the last broadcasted forwarding table.
		self.past_forwarding = ForwardingTable()

	def add_static_route(self, host, port):
		"""
		Adds a static route to a host directly connected to this router.

		Called automatically by the framework whenever a host is connected
		to this router.

		:param host: the host.
		:param port: the port that the host is attached to.
		:returns: nothing.
		"""
		# `port` should have been added to `peer_tables` by `handle_link_up`
		# when the link came up.
		assert port in self.peer_tables, "Link is not up?"

		# TODO: fill this in!
		self.peer_tables[port][host] = PeerTableEntry(host, 0, PeerTableEntry.FOREVER)

		self.update_forwarding_table()
		self.send_routes(force=False)

	def handle_link_up(self, port, latency):
		"""
		Called by the framework when a link attached to this router goes up.

		:param port: the port that the link is attached to.
		:param latency: the link latency.
		:returns: nothing.
		"""
		self.link_latency[port] = latency
		self.peer_tables[port] = PeerTable()

		# TODO: fill in the rest!
		self.history[port] = {}

		for dst, entry in self.forwarding_table.items():
			latency = min(entry.latency, INFINITY)
			self.history[port][dst] = latency
			self.send(basics.RoutePacket(dst, latency), port)

	def handle_link_down(self, port):
		"""
		Called by the framework when a link attached to this router does down.

		:param port: the port number used by the link.
		:returns: nothing.
		"""
		# TODO: fill this in!
		del self.link_latency[port]
		del self.peer_tables[port]

		self.update_forwarding_table()
		self.send_routes(force=False)

	def handle_route_advertisement(self, dst, port, route_latency):
		"""
		Called when the router receives a route advertisement from a neighbor.

		:param dst: the destination of the advertised route.
		:param port: the port that the advertisement came from.
		:param route_latency: latency from the neighbor to the destination.
		:return: nothing.
		"""
		# TODO: fill this in!
		self.peer_tables[port][dst] = PeerTableEntry(dst, route_latency, api.current_time() + ROUTE_TTL)

		self.update_forwarding_table()
		self.send_routes(force=False)

	def update_forwarding_table(self):
		"""
		Computes and stores a new forwarding table merged from all peer tables.

		:returns: nothing.
		"""
		self.forwarding_table.clear()  # First, clear the old forwarding table.

		# TODO: populate `self.forwarding_table` by combining peer tables.
		for peer, peer_table in self.peer_tables.items():
			for dst, entry in peer_table.items():
				latency = self.link_latency[peer] + entry.latency

				if dst not in self.forwarding_table or latency < self.forwarding_table[dst].latency:
					self.forwarding_table[dst] = ForwardingTableEntry(dst, peer, latency)

	def handle_data_packet(self, packet, in_port):
		"""
		Called when a data packet arrives at this router.

		You may want to forward the packet, drop the packet, etc. here.

		:param packet: the packet that arrived.
		:param in_port: the port from which the packet arrived.
		:return: nothing.
		"""
		# TODO: fill this in!
		dst = packet.dst

		if dst in self.forwarding_table:
			entry = self.forwarding_table[dst]

			if entry.latency < INFINITY and entry.port != in_port:
				self.send(packet, entry.port)

	def send_routes(self, force=False):
		"""
		Send route advertisements for all routes in the forwarding table.

		:param force: if True, advertises ALL routes in the forwarding table;
					  otherwise, advertises only those routes that have
					  changed since the last advertisement.
		:return: nothing.
		"""
		# TODO: fill this in!
		if self.POISON_MODE:
			for dst in self.past_forwarding.keys():
				if dst not in self.forwarding_table:
					self.forwarding_table[dst] = ForwardingTableEntry(dst, self.link_latency.keys()[0], INFINITY)

		if force:
			self.history = {}

			for port in self.link_latency.keys():
				self.history[port] = {}

				for dst, entry in self.forwarding_table.items():
					if port != entry.port:
						latency = min(entry.latency, INFINITY)
						self.history[port][dst] = latency
						self.send(basics.RoutePacket(dst, latency), port)
		else:
			for port in self.link_latency.keys():
				if port not in self.history:
					self.history[port] = {}

				for dst, entry in self.forwarding_table.items():
					if port != entry.port:
						latency = min(entry.latency, INFINITY)

						if dst not in self.history[port] or self.history[port][dst] != latency:
							self.history[port][dst] = latency
							self.send(basics.RoutePacket(dst, latency), port)

		if self.POISON_MODE:
			for dst, entry in self.forwarding_table.items():
				if entry.port not in self.history:
					self.history[entry.port] = {}

				if dst not in self.history[entry.port] or self.history[entry.port][dst] != INFINITY:
					self.history[entry.port][dst] = INFINITY
					self.send(basics.RoutePacket(dst, INFINITY), entry.port)

		self.past_forwarding.clear()

		for dst, entry in self.forwarding_table.items():
			self.past_forwarding[dst] = ForwardingTableEntry(dst, entry.port, entry.latency)

	def expire_routes(self):
		"""
		Clears out expired routes from peer tables; updates forwarding table
		accordingly.
		"""
		# TODO: fill this in!
		update = False

		for peer, peer_table in self.peer_tables.items():
			for dst, entry in peer_table.items():
				if entry.expire_time < api.current_time():
					update = True
					del peer_table[dst]

		if update:
			self.update_forwarding_table()

	def handle_timer(self):
		"""
		Called periodically.

		This function simply calls helpers to clear out expired routes and to
		send the forwarding table to neighbors.
		"""
		self.expire_routes()
		self.send_routes(force=True)

	# Feel free to add any helper methods!
